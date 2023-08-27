package ru.mrgrd56.api.controllers

import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import ru.mrgrd56.api.utils.ifNullOrBlank
import java.time.Instant
import javax.servlet.http.HttpServletRequest

@RestController
class HomeController {
    private val log = LoggerFactory.getLogger(this.javaClass)

    @GetMapping(value = ["ip"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getIpAddress(request: HttpServletRequest): String {
        val realIp = request.getHeader("X-Real-IP")
        return realIp ifNullOrBlank {
            request.remoteAddr
        }
    }

    @GetMapping(value = ["time", "time/iso"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getTimeAsIsoString(): String = Instant.now().toString()

    @GetMapping(value = ["time/epoch"], produces = [MediaType.TEXT_PLAIN_VALUE])
    fun getTimeAsEpochMillis(): String = Instant.now().toEpochMilli().toString()
}
