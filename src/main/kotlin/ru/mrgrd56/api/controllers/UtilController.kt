package ru.mrgrd56.api.controllers

import org.apache.commons.lang3.ObjectUtils
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

@RestController
@RequestMapping("util")
class UtilController {
    @RequestMapping(value = ["delay"], method = [RequestMethod.GET, RequestMethod.POST])
    @Throws(InterruptedException::class)
    fun delay(@RequestParam ms: Long): String {
        Thread.sleep(ms)
        return Objects.toString(Math.random())
    }

    @RequestMapping(value = ["http-response"], method = [RequestMethod.GET, RequestMethod.POST])
    fun getHttpResponse(
        @RequestParam(required = false, defaultValue = "200") status: Int?,
        @RequestParam(required = false) header: List<String>?,
        @RequestParam(required = false) body: Any
    ): ResponseEntity<*> {
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
}
