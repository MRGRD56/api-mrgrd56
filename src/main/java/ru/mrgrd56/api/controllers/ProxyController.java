package ru.mrgrd56.api.controllers;

import io.netty.handler.codec.http.HttpHeaders;
import org.asynchttpclient.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mrgrd56.api.utils.ValueStreamingResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.SynchronousQueue;

@RestController
@RequestMapping("proxy")
public class ProxyController {
    private final AsyncHttpClient asyncHttpClient;

    public ProxyController(AsyncHttpClient asyncHttpClient) {
        this.asyncHttpClient = asyncHttpClient;
    }

    @RequestMapping
    public ResponseEntity<StreamingResponseBody> proxy(
            @RequestParam String url,
            @RequestHeader org.springframework.http.HttpHeaders requestHeadersIn,
            HttpServletRequest requestIn) throws Exception {
        String proxyPath = UriComponentsBuilder.newInstance()
                .scheme(requestIn.getScheme())
                .host(requestIn.getServerName())
                .port(requestIn.getServerPort())
                .path(requestIn.getServletPath())
                .toUriString();

        UriComponents proxiedUri = UriComponentsBuilder.fromUriString(url).build();
        InetAddress proxiedAddress = InetAddress.getByName(proxiedUri.getHost());
        if (proxiedAddress.isSiteLocalAddress() || proxiedAddress.isLoopbackAddress()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header("X-MRGRD56-Proxy-Response", requestIn.getServerName())
                    .body(ValueStreamingResponseBody.ofString("You're not allowed to access the local network of the server"));
        }

        org.springframework.http.HttpHeaders requestHeaders = new org.springframework.http.HttpHeaders();
        requestHeaders.addAll(requestHeadersIn);
        requestHeaders.remove("host");

        Request request = new RequestBuilder()
                .setMethod(requestIn.getMethod())
                .setUrl(url)
                .setHeaders(requestHeaders)
                .build();

        BlockingQueue<HttpResponseStatus> responseStatusQueue = new SynchronousQueue<>();
        BlockingQueue<HttpHeaders> responseHeadersQueue = new SynchronousQueue<>();

        PipedOutputStream pipedOutputStream = new PipedOutputStream();
        PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);

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
                throwable.printStackTrace(System.err);
            }

            @Override
            public Object onCompleted() throws Exception {
                pipedOutputStream.flush();
                pipedOutputStream.close();
                return null;
            }
        });

        StreamingResponseBody streamingResponseBody = (outputStream) -> {
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
            pipedInputStream.transferTo(bufferedOutputStream);
            bufferedOutputStream.flush();
            outputStream.close();
        };

        HttpResponseStatus responseStatus = Objects.requireNonNull(responseStatusQueue.take());
        HttpHeaders responseHeaders = Objects.requireNonNull(responseHeadersQueue.take());

        org.springframework.http.HttpHeaders springResponseHeaders = new org.springframework.http.HttpHeaders();
        for (String header : responseHeaders.names()) {
            List<String> headerValues = responseHeaders.getAll(header);

            if (header.equalsIgnoreCase("location")) {
                headerValues = headerValues.stream()
                        .map(location -> {
                            return UriComponentsBuilder.fromUriString(proxyPath)
                                    .queryParam("url", location)
                                    .toUriString();
                        })
                        .toList();
            }

            springResponseHeaders.addAll(header, headerValues);
        }

        return ResponseEntity.status(responseStatus.getStatusCode())
                .headers(springResponseHeaders)
                .body(streamingResponseBody);
    }
}
