package ru.mrgrd56.api.proxy;

import io.netty.handler.codec.http.HttpHeaders;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.asynchttpclient.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mrgrd56.api.utils.ValueStreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

@Service
public class ProxyService {
    private static final String X_MRGRD56_PROXY_RESPONSE = "X-MRGRD56-Proxy-Response";

    private final AsyncHttpClient asyncHttpClient;

    public ProxyService(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    public ResponseEntity<StreamingResponseBody> proxyRequest(
            String url, org.springframework.http.HttpHeaders requestHeadersIn, HttpServletRequest requestIn) {
        try {
            String proxyPath = getProxyPath(requestIn);
            String requestedHost = getRequestedHost(url);

            ResponseEntity<StreamingResponseBody> errorResponse = validateUrl(url, requestIn);
            if (errorResponse != null) {
                return errorResponse;
            }

            org.springframework.http.HttpHeaders requestHeaders = getRequestHeaders(requestHeadersIn);

            List<String> referers = getRequestReferers(requestHeaders);

            Request request = new RequestBuilder()
                    .setMethod(requestIn.getMethod())
                    .setUrl(url)
                    .setHeaders(requestHeaders)
                    .build();

            BlockingQueue<HttpResponseStatus> responseStatusQueue = new SynchronousQueue<>();
            BlockingQueue<HttpHeaders> responseHeadersQueue = new SynchronousQueue<>();

            PipedOutputStream pipedOutputStream = new PipedOutputStream();
            PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

            sendProxiedRequest(request, responseStatusQueue, responseHeadersQueue, pipedOutputStream);

            StreamingResponseBody streamingResponseBody = createStreamingResponseBody(pipedInputStream);

            HttpResponseStatus responseStatus = Objects.requireNonNull(responseStatusQueue.take());
            org.springframework.http.HttpHeaders springResponseHeaders = getResponseHeaders(responseHeadersQueue, requestedHost, proxyPath, referers);

            return ResponseEntity.status(responseStatus.getStatusCode())
                    .headers(springResponseHeaders)
                    .body(streamingResponseBody);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .header(X_MRGRD56_PROXY_RESPONSE, requestIn.getServerName())
                    .body(outputStream -> {
                        e.printStackTrace(new PrintStream(outputStream, true, StandardCharsets.UTF_8));
                    });
        }
    }

    private void sendProxiedRequest(Request request, BlockingQueue<HttpResponseStatus> responseStatusQueue, BlockingQueue<HttpHeaders> responseHeadersQueue, PipedOutputStream pipedOutputStream) {
        asyncHttpClient.prepareRequest(request).execute(new AsyncHandler<>() {
            @Override
            public State onStatusReceived(HttpResponseStatus httpResponseStatus) throws Exception {
                responseStatusQueue.put(httpResponseStatus);
                return State.CONTINUE;
            }

            @Override
            public State onHeadersReceived(HttpHeaders httpHeaders) throws Exception {
                responseHeadersQueue.put(httpHeaders);
                return State.CONTINUE;
            }

            @Override
            public State onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
                pipedOutputStream.write(bodyPart.getBodyPartBytes(), 0, bodyPart.length());
                if (bodyPart.isLast()) {
                    pipedOutputStream.flush();
                    pipedOutputStream.close();
                }
                return State.CONTINUE;
            }

            @Override
            public void onThrowable(Throwable throwable) {
                // TODO handle exceptions
                throwable.printStackTrace(System.err);
            }

            @Override
            public Object onCompleted() throws Exception {
                pipedOutputStream.flush();
                pipedOutputStream.close();
                return null;
            }
        });
    }

    private ResponseEntity<StreamingResponseBody> validateUrl(String url, HttpServletRequest requestIn) throws UnknownHostException {
        UriComponents proxiedUri = UriComponentsBuilder.fromUriString(url).build();
        if (proxiedUri.getHost() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .header(X_MRGRD56_PROXY_RESPONSE, requestIn.getServerName())
                    .body(ValueStreamingResponseBody.ofString("Invalid url specified"));
        }

        InetAddress proxiedAddress = InetAddress.getByName(proxiedUri.getHost());
        if (proxiedAddress.isSiteLocalAddress() || proxiedAddress.isLoopbackAddress()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(X_MRGRD56_PROXY_RESPONSE, requestIn.getServerName())
                    .body(ValueStreamingResponseBody.ofString("You're not allowed to access the local network of the server"));
        }

        return null;
    }

    private StreamingResponseBody createStreamingResponseBody(PipedInputStream pipedInputStream) {
        return (outputStream) -> {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            pipedInputStream.transferTo(bufferedOutputStream);
            bufferedOutputStream.flush();
            outputStream.close();
        };
    }

    private org.springframework.http.HttpHeaders getResponseHeaders(BlockingQueue<HttpHeaders> responseHeadersQueue, String requestedHost, String proxyPath, List<String> referers) throws InterruptedException {
        HttpHeaders responseHeaders = Objects.requireNonNull(responseHeadersQueue.take());

        org.springframework.http.HttpHeaders springResponseHeaders = new org.springframework.http.HttpHeaders();
        for (String header : responseHeaders.names()) {
            List<String> headerValues = responseHeaders.getAll(header);

            if (header.equalsIgnoreCase("location")) {
                headerValues = headerValues.stream()
                        .map(location -> {
                            String actualLocation = location;
                            UriComponents locationComponents = UriComponentsBuilder.fromUriString(location).build();

                            if (StringUtils.isBlank(locationComponents.getHost())) {
                                actualLocation = UriComponentsBuilder.fromUriString(requestedHost)
                                        .replacePath(location)
                                        .toUriString();
                            }

                            return UriComponentsBuilder.fromUriString(proxyPath)
                                    .queryParam("url", actualLocation)
                                    .toUriString();
                        })
                        .toList();
            }

            springResponseHeaders.addAll(header, headerValues);
        }

        if (CollectionUtils.isNotEmpty(referers)) {
            springResponseHeaders.addAll("Access-Control-Allow-Origin", referers);
        }

        springResponseHeaders.add("Transfer-Encoding", "chunked");

        return springResponseHeaders;
    }

    private List<String> getRequestReferers(org.springframework.http.HttpHeaders requestHeaders) {
        return Optional.ofNullable(requestHeaders.get("referer"))
                .or(() -> Optional.ofNullable(requestHeaders.get("origin")))
                .map(refererList -> {
                    return refererList.stream()
                            .map(referer -> {
                                return UriComponentsBuilder.fromUriString(referer)
                                        .replacePath("")
                                        .toUriString();
                            })
                            .toList();
                })
                .orElse(null);
    }

    private org.springframework.http.HttpHeaders getRequestHeaders(org.springframework.http.HttpHeaders requestHeadersIn) {
        org.springframework.http.HttpHeaders requestHeaders = new org.springframework.http.HttpHeaders();
        requestHeaders.addAll(requestHeadersIn);
        requestHeaders.remove("host");
        return requestHeaders;
    }

    private String getRequestedHost(String url) {
        return UriComponentsBuilder.fromUriString(url)
                .replacePath(null)
                .replaceQuery(null)
                .fragment(null)
                .toUriString();
    }

    private String getProxyPath(HttpServletRequest requestIn) {
        return UriComponentsBuilder.newInstance()
                .scheme(requestIn.getScheme())
                .host(requestIn.getServerName())
                .port(requestIn.getServerPort())
                .path(requestIn.getServletPath())
                .toUriString();
    }
}
