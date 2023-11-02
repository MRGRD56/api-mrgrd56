package ru.mrgrd56.api.controllers

import org.apache.commons.lang3.ObjectUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import ru.mrgrd56.api.requestlogging.model.RequestLogDto
import ru.mrgrd56.api.requestlogging.services.RequestLoggingService
import ru.mrgrd56.api.utils.logger
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("util")
class UtilController(
    private val requestLoggingService: RequestLoggingService
) {
    private val log = logger(this::class.java)

    @RequestMapping(value = ["delay"], method = [RequestMethod.GET, RequestMethod.POST])
    @Throws(InterruptedException::class)
    fun delay(@RequestParam ms: Long): String {
        Thread.sleep(ms)
        return Objects.toString(Math.random())
    }

    @RequestMapping(value = ["http-response"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getHttpResponse(
        request: HttpServletRequest,
        @RequestParam(required = false, defaultValue = "200") status: Int?,
        @RequestParam(required = false) header: List<String>?,
        @RequestParam(required = false) body: Any,
        @RequestParam(defaultValue = "false") log: Boolean
    ): ResponseEntity<*> {
        this.log.info("getHttpResponse: status={}, headers={}, body={}", status, header, body)

        if (log) {
            try {
                requestLoggingService.logRequest(request, body)
            } catch (e: Exception) {
                this.log.warn("getHttpResponse: Unable to log a request: {}", e.message, e)
            }
        }

        val responseStatus = status?.let { HttpStatus.valueOf(it) } ?: HttpStatus.OK
        val responseHeaders = HttpHeaders()

        header?.let {
            for (rawHeader in header) {
                val parts = rawHeader.split(":\\n?".toRegex(), limit = 2)
                if (parts.size != 2) {
                    return ResponseEntity.badRequest().body("Invalid header '$rawHeader'")
                }
                responseHeaders.add(ObjectUtils.requireNonEmpty(parts[0]), parts[1])
            }
        }

        return ResponseEntity(body, responseHeaders, responseStatus)
    }

    @RequestMapping(value = ["log-request"])
    fun logRequest(request: HttpServletRequest) {
        requestLoggingService.logRequest(request)
    }

    @GetMapping("log-request/logs")
    fun getLoggedRequests(): List<RequestLogDto> {
        return requestLoggingService.getLoggedRequests()
    }
}
