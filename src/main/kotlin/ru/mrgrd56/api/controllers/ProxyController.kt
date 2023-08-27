package ru.mrgrd56.api.controllers

import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import ru.mrgrd56.api.proxy.ProxyService
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("proxy")
class ProxyController(private val proxyService: ProxyService) {
    @RequestMapping
    fun proxy(
        @RequestParam url: String,
        @RequestHeader requestHeadersIn: HttpHeaders,
        requestIn: HttpServletRequest
    ): ResponseEntity<StreamingResponseBody> {
        return proxyService.proxyRequest(url, requestHeadersIn, requestIn)
    }
}
