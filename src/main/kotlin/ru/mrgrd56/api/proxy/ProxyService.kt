package ru.mrgrd56.api.proxy

import org.asynchttpclient.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import ru.mrgrd56.api.utils.ValueStreamingResponseBody.ofString
import java.io.*
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets
import java.util.*
import java.util.concurrent.BlockingQueue
import java.util.concurrent.SynchronousQueue
import javax.servlet.http.HttpServletRequest
import io.netty.handler.codec.http.HttpHeaders as NettyHttpHeaders
import org.springframework.http.HttpHeaders as SpringHttpHeaders

@Service
class ProxyService(private val asyncHttpClient: AsyncHttpClient) {
    fun proxyRequest(
        url: String, requestHeadersIn: SpringHttpHeaders, requestIn: HttpServletRequest
    ): ResponseEntity<StreamingResponseBody> {
        return try {
            val proxyPath: String = getProxyPath(requestIn)
            val requestedHost: String = getRequestedHost(url)
            val errorResponse: ResponseEntity<StreamingResponseBody>? = validateUrl(url, requestIn)

            if (errorResponse != null) {
                return errorResponse
            }

            val requestHeaders: SpringHttpHeaders = getRequestHeaders(requestHeadersIn)
            val referers: List<String>? = getRequestReferers(requestHeaders)

            val request = RequestBuilder()
                .setMethod(requestIn.method)
                .setUrl(url)
                .setHeaders(requestHeaders)
                .build()

            val responseStatusQueue: BlockingQueue<HttpResponseStatus> = SynchronousQueue()
            val responseHeadersQueue: BlockingQueue<NettyHttpHeaders> = SynchronousQueue()

            val pipedOutputStream = PipedOutputStream()
            val pipedInputStream = PipedInputStream(pipedOutputStream)

            sendProxiedRequest(request, responseStatusQueue, responseHeadersQueue, pipedOutputStream)
            val streamingResponseBody = createStreamingResponseBody(pipedInputStream)
            val responseStatus = Objects.requireNonNull(responseStatusQueue.take())
            val springResponseHeaders = getResponseHeaders(responseHeadersQueue, requestedHost, proxyPath, referers)
            ResponseEntity.status(responseStatus.statusCode)
                .headers(springResponseHeaders)
                .body(streamingResponseBody)
        } catch (e: Exception) {
            ResponseEntity.internalServerError()
                .header(X_MRGRD56_PROXY_RESPONSE, requestIn.serverName)
                .body(StreamingResponseBody { outputStream: OutputStream? ->
                    e.printStackTrace(
                        PrintStream(
                            outputStream,
                            true,
                            StandardCharsets.UTF_8
                        )
                    )
                })
        }
    }

    private fun sendProxiedRequest(
        request: Request,
        responseStatusQueue: BlockingQueue<HttpResponseStatus>,
        responseHeadersQueue: BlockingQueue<NettyHttpHeaders>,
        pipedOutputStream: PipedOutputStream
    ) {
        asyncHttpClient.prepareRequest(request).execute(object : AsyncHandler<Any?> {
            @Throws(Exception::class)
            override fun onStatusReceived(httpResponseStatus: HttpResponseStatus): AsyncHandler.State {
                responseStatusQueue.put(httpResponseStatus)
                return AsyncHandler.State.CONTINUE
            }

            @Throws(Exception::class)
            override fun onHeadersReceived(httpHeaders: NettyHttpHeaders): AsyncHandler.State {
                responseHeadersQueue.put(httpHeaders)
                return AsyncHandler.State.CONTINUE
            }

            @Throws(Exception::class)
            override fun onBodyPartReceived(bodyPart: HttpResponseBodyPart): AsyncHandler.State {
                pipedOutputStream.write(bodyPart.bodyPartBytes, 0, bodyPart.length())
                if (bodyPart.isLast) {
                    pipedOutputStream.flush()
                    pipedOutputStream.close()
                }
                return AsyncHandler.State.CONTINUE
            }

            override fun onThrowable(throwable: Throwable) {
                // TODO handle exceptions
                throwable.printStackTrace(System.err)
            }

            @Throws(Exception::class)
            override fun onCompleted(): Any? {
                pipedOutputStream.flush()
                pipedOutputStream.close()
                return null
            }
        })
    }

    @Throws(UnknownHostException::class)
    private fun validateUrl(url: String, requestIn: HttpServletRequest): ResponseEntity<StreamingResponseBody>? {
        val proxiedUri = UriComponentsBuilder.fromUriString(url).build()
        if (proxiedUri.host == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .header(X_MRGRD56_PROXY_RESPONSE, requestIn.serverName)
                .body(ofString("Invalid url specified"))
        }

        val proxiedAddress = InetAddress.getByName(proxiedUri.host)
        if (proxiedAddress.isSiteLocalAddress || proxiedAddress.isLoopbackAddress) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .header(X_MRGRD56_PROXY_RESPONSE, requestIn.serverName)
                .body(ofString("You're not allowed to access the local network of the server"))
        }

        return null
    }

    private fun createStreamingResponseBody(pipedInputStream: PipedInputStream): StreamingResponseBody {
        return StreamingResponseBody { outputStream: OutputStream ->
            val bufferedOutputStream = BufferedOutputStream(outputStream)
            pipedInputStream.transferTo(bufferedOutputStream)
            bufferedOutputStream.flush()
            outputStream.close()
        }
    }

    @Throws(InterruptedException::class)
    private fun getResponseHeaders(
        responseHeadersQueue: BlockingQueue<NettyHttpHeaders>,
        requestedHost: String,
        proxyPath: String,
        referers: List<String>?
    ): SpringHttpHeaders {
        val responseHeaders = responseHeadersQueue.take()
        val springResponseHeaders = SpringHttpHeaders()
        for (header in responseHeaders.names()) {
            var headerValues = responseHeaders.getAll(header)

            if (header.equals("location", ignoreCase = true)) {
                headerValues = headerValues.map { location: String ->
                    UriComponentsBuilder.fromUriString(location).build()
                        .let { locationComponents: UriComponents ->
                            if (locationComponents.host.isNullOrBlank()) {
                                UriComponentsBuilder.fromUriString(requestedHost)
                                    .replacePath(location)
                                    .toUriString()
                            } else {
                                location
                            }
                        }
                        .let { actualLocation: String ->
                            UriComponentsBuilder.fromUriString(proxyPath)
                                .queryParam("url", actualLocation)
                                .toUriString()
                        }
                }
            }

            springResponseHeaders.addAll(header, headerValues)
        }

        if (!referers.isNullOrEmpty()) {
            springResponseHeaders.addAll("Access-Control-Allow-Origin", referers)
        }

        springResponseHeaders.add("Transfer-Encoding", "chunked")
        return springResponseHeaders
    }

    private fun getRequestReferers(requestHeaders: SpringHttpHeaders): List<String>? {
        val referers = requestHeaders["referer"] ?: requestHeaders["origin"]

        return referers?.map {
            UriComponentsBuilder.fromUriString(it)
                .replacePath("")
                .toUriString()
        }
    }

    private fun getRequestHeaders(requestHeadersIn: SpringHttpHeaders): SpringHttpHeaders {
        return SpringHttpHeaders().apply {
            addAll(requestHeadersIn)
            remove("host")
        }
    }

    private fun getRequestedHost(url: String): String {
        return UriComponentsBuilder.fromUriString(url)
            .replacePath(null)
            .replaceQuery(null)
            .fragment(null)
            .toUriString()
    }

    private fun getProxyPath(requestIn: HttpServletRequest): String {
        return UriComponentsBuilder.newInstance()
            .scheme(requestIn.scheme)
            .host(requestIn.serverName)
            .port(requestIn.serverPort)
            .path(requestIn.servletPath)
            .toUriString()
    }

    companion object {
        private const val X_MRGRD56_PROXY_RESPONSE = "X-MRGRD56-Proxy-Response"
    }
}
