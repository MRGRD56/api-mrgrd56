package ru.mrgrd56.api.controllers

import freemarker.template.Configuration
import org.apache.commons.lang3.ObjectUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.mrgrd56.api.requestlogging.model.RequestLogDto
import ru.mrgrd56.api.requestlogging.services.RequestLoggingService
import ru.mrgrd56.api.utils.fill
import ru.mrgrd56.api.utils.logger
import java.util.*
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("util")
class UtilController(
    private val requestLoggingService: RequestLoggingService,
    private val freemarker: Configuration,
) {
    private val log = logger(this::class.java)

    @RequestMapping("delay", method = [RequestMethod.GET, RequestMethod.POST])
    @Throws(InterruptedException::class)
    fun delay(@RequestParam ms: Long): String {
        Thread.sleep(ms)
        return Objects.toString(Math.random())
    }

    @RequestMapping("http-response", method = [RequestMethod.GET, RequestMethod.POST])
    fun getHttpResponse(
        request: HttpServletRequest,
        @RequestParam(required = false, defaultValue = "200") status: Int?,
        @RequestParam(required = false) header: List<String>?,
        @RequestParam(required = false) body: Any,
        @RequestParam(required = false) log: String?,
        @RequestParam(name = "log.keepPersonal", defaultValue = "true") keepPersonal: Boolean,
        @RequestParam(name = "log.keepCredentials", defaultValue = "false") keepCredentials: Boolean,
        @RequestParam(name = "log.hideHeaders", defaultValue = "") hideHeaders: Set<String>
    ): ResponseEntity<*> {
        this.log.info("getHttpResponse: status={}, headers={}, body={}", status, header, body)

        if (!log.isNullOrBlank()) {
            try {
                requestLoggingService.logRequest(log, request, keepPersonal, keepCredentials, hideHeaders)
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

    @RequestMapping("log-request/{loggerId}")
    fun logRequest(request: HttpServletRequest,
                   @PathVariable loggerId: String,
                   @RequestParam(defaultValue = "true") keepPersonal: Boolean,
                   @RequestParam(defaultValue = "false") keepCredentials: Boolean,
                   @RequestParam(defaultValue = "") hideHeaders: Set<String>) {
        requestLoggingService.logRequest(loggerId, request, keepPersonal, keepCredentials, hideHeaders)
    }

    @GetMapping("log-request/{loggerId}/logs.json")
    fun getLoggedRequests(@PathVariable loggerId: String): List<RequestLogDto> {
        return requestLoggingService.getLoggedRequests(loggerId)
    }

    @GetMapping("log-request/{loggerId}/logs",
        produces = [MediaType.TEXT_HTML_VALUE]
    )
    fun viewLoggedRequests(@PathVariable loggerId: String): String {
        val template = freemarker.getTemplate("request-logs.html.ftl")

        return template.fill(mapOf(
            "requests" to requestLoggingService.getLoggedRequests(loggerId)
        ))
    }

    @RequestMapping("log-request/{loggerId}/clear",
        method = [RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE]
    )
    fun clearLoggerRequests(@PathVariable loggerId: String) {
        requestLoggingService.clearLoggedRequests(loggerId)
    }
}
