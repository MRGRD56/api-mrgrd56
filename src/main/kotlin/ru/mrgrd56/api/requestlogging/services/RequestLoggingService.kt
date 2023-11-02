package ru.mrgrd56.api.requestlogging.services

import org.apache.commons.collections4.queue.CircularFifoQueue
import org.springframework.stereotype.Service
import ru.mrgrd56.api.requestlogging.model.RequestLogDto
import ru.mrgrd56.api.utils.logger
import java.nio.charset.StandardCharsets
import java.time.Instant
import javax.servlet.http.HttpServletRequest

@Service
class RequestLoggingService {
    private val log = logger(this::class.java)

    private val requests = CircularFifoQueue<RequestLogDto>(50)

    fun logRequest(request: HttpServletRequest) {
        logRequest(
            request,
            body = request.inputStream.readNBytes(1 * 1024 * 1024).toString(StandardCharsets.UTF_8)
        )
    }

    fun logRequest(request: HttpServletRequest, body: Any?) {
        val requestLog = RequestLogDto(
            time = Instant.now(),
            method = request.method,
            path = request.requestURI,
            query = request.queryString,
            headers = request.headerNames.asSequence().associateWith { request.getHeader(it) },
            body = body.toString()
        )

        log.info("logRequest: {}", requestLog);

        requests += requestLog
    }

    fun getLoggedRequests(): List<RequestLogDto> {
        return requests.reversed()
    }
}