package ru.mrgrd56.api.controllers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Objects;

@RestController
public class HomeController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping(value = "ip", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getIpAddress(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    @GetMapping(value = {"time", "time/iso"}, produces = MediaType.TEXT_PLAIN_VALUE)
    public String getTimeAsIsoString() {
        return Instant.now().toString();
    }

    @GetMapping(value = "time/epoch", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getTimeAsEpochMillis() {
        return Objects.toString(Instant.now().toEpochMilli());
    }
}
