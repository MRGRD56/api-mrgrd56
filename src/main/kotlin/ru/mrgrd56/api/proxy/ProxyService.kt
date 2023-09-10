package ru.mrgrd56.api.proxy

import org.asynchttpclient.*
import org.slf4j.LoggerFactory
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

private const val X_MRGRD56_PROXY_RESPONSE = "X-MRGRD56-Proxy-Response"

@Service
class ProxyService(private val asyncHttpClient: AsyncHttpClient) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun proxyRequest(
        originalUrl: String, requestHeadersIn: SpringHttpHeaders, requestIn: HttpServletRequest
    ): ResponseEntity<StreamingResponseBody> {
        log.info("Proxying: [{}]", originalUrl)

        return try {
            val url: String = prepareTargetUrl(originalUrl)

            val proxyPath: String = getProxyPath(requestIn)
            val requestedHost: String = getRequestedHost(url)
            val errorResponse: ResponseEntity<StreamingResponseBody>? = validateUrl(url, requestIn)

            if (errorResponse != null) {
                return errorResponse
            }

            val requestHeaders: SpringHttpHeaders = getRequestHeaders(requestHeadersIn)
            val requestOrigins: List<String> = getRequestOrigins(requestHeaders)

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
            val springResponseHeaders =
                getResponseHeaders(responseHeadersQueue.take(), requestedHost, proxyPath, requestOrigins)
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

        if (!proxiedUri.host!!.equals("api-mrgrd56", ignoreCase = true) || proxiedUri.port != 8080) {
            val proxiedAddress = InetAddress.getByName(proxiedUri.host)

            if (proxiedAddress.isSiteLocalAddress || proxiedAddress.isLoopbackAddress) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .header(X_MRGRD56_PROXY_RESPONSE, requestIn.serverName)
                    .body(ofString("You're not allowed to access the local network of the server"))
            }
        }

        return null
    }

    private fun createStreamingResponseBody(pipedInputStream: PipedInputStream): StreamingResponseBody {
        return StreamingResponseBody { outputStream: OutputStream ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (pipedInputStream.read(buffer, 0, buffer.size).also { bytesRead = it } != -1) {
                outputStream.write(buffer, 0, bytesRead)
                outputStream.flush()
            }

            outputStream.close()
        }
    }

    val ignoredHeaders = hashSetOf("content-length", "transfer-encoding", "connection")

    @Throws(InterruptedException::class)
    private fun getResponseHeaders(
        responseHeaders: NettyHttpHeaders,
        requestedHost: String,
        proxyPath: String,
        requestOrigins: List<String>
    ): SpringHttpHeaders {
        val springResponseHeaders = SpringHttpHeaders()
        for (header in responseHeaders.names()) {
            val headerName = header.lowercase()

            if (headerName in ignoredHeaders) {
                continue
            }

            var headerValues = responseHeaders.getAll(header)

            if (headerName == "location") {
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

        if (requestOrigins.isNotEmpty()) {
            springResponseHeaders["Access-Control-Allow-Origin"] = requestOrigins
        }

        return springResponseHeaders
    }

    private fun getRequestOrigins(requestHeaders: SpringHttpHeaders): List<String> {
        val referers = sequence(requestHeaders["referer"]) + sequence(requestHeaders["origin"])

        return referers
            .filter { !it.isNullOrEmpty() }
            .map {
                UriComponentsBuilder.fromUriString(it)
                    .replacePath(null)
                    .replaceQuery(null)
                    .fragment(null)
                    .toUriString()
                    .ifEmpty { it }
            }
            .take(1)
            .toList()
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

    private fun prepareTargetUrl(url: String): String {
        val uriComponents = UriComponentsBuilder.fromUriString(url).build()

        if ("api.mrgrd56.ru".equals(uriComponents.host, ignoreCase = true)) {
            return UriComponentsBuilder.fromUriString(url)
                .scheme("http")
                .host("api-mrgrd56")
                .port(8080)
                .toUriString()
        }

        return url
    }
}

private fun <T> sequence(items: Iterable<T>?): Sequence<T> {
    return items?.asSequence() ?: emptySequence()
}