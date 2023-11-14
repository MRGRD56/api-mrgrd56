package ru.mrgrd56.api.requestlogging.services

import org.apache.commons.collections4.queue.CircularFifoQueue
import org.apache.commons.lang3.StringUtils
import org.springframework.stereotype.Service
import ru.mrgrd56.api.requestlogging.model.RequestLogDto
import ru.mrgrd56.api.utils.logger
import java.nio.charset.StandardCharsets
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import javax.servlet.http.HttpServletRequest

private val PERSONAL_HEADERS = setOf(
    "referer",
    "x-forwarded-for",
    "x-real-ip",
    "if-modified-since",
    "if-none-match"
)

private val SENSITIVE_HEADERS = setOf(
    "authorization",
    "cookie",
    "proxy-authorization",
    "set-cookie",
    "x-api-key",
    "x-auth-token",
    "x-csrf-token",
    "x-xsrf-token"
)

@Service
class RequestLoggingService {
    private val log = logger(this::class.java)

    private val requestsByLoggers = ConcurrentHashMap<String, CircularFifoQueue<RequestLogDto>>()

    fun logRequest(
        loggerId: String,
        request: HttpServletRequest,
        keepPersonal: Boolean,
        keepCredentials: Boolean,
        hideHeaders: Set<String>,
        body: Any? = request.inputStream.readNBytes(1 * 1024 * 1024).toString(StandardCharsets.UTF_8)
    ) {
        val requestLog = RequestLogDto(
            id = UUID.randomUUID(),
            time = Instant.now(),
            method = request.method,
            path = request.requestURI,
            query = request.queryString,
            headers = request.headerNames.asSequence()
                .associate {
                    formatHeaderName(it) to
                            when {
                                !keepPersonal && it in PERSONAL_HEADERS -> {
                                    "[HIDDEN]"
                                }
                                !keepCredentials && it in SENSITIVE_HEADERS -> {
                                    "[HIDDEN]"
                                }
                                it in hideHeaders -> {
                                    "[HIDDEN]"
                                }
                                else -> {
                                    request.getHeader(it)
                                }
                            }
                },
            body = body.toString()
        )

        log.info("logRequest: {}", requestLog);

        val requests = requestsByLoggers.computeIfAbsent(loggerId) { CircularFifoQueue(100) }
        requests += requestLog
    }

    fun getLoggedRequests(loggerId: String): List<RequestLogDto> {
        return requestsByLoggers[loggerId]?.reversed() ?: emptyList()
    }

    private fun formatHeaderName(name: String): String {
        return name.split('-').asSequence()
            .map { StringUtils.capitalize(it) }
            .joinToString("-")
    }

    fun clearLoggedRequests(loggerId: String) {
        requestsByLoggers[loggerId]?.clear()
    }
}