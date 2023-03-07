package ru.mrgrd56.api.controllers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;

@RestController
public class HomeController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @GetMapping("ip")
    public String getIpAddress(
            @RequestHeader(value = "X-Real-IP", required = false) String realIp,
            @RequestHeader Map<String, String> headers,
            HttpServletRequest request) {
        log.info("/ip headers {}", headers);
        log.info("/ip header X-Real-IP {}", request.getHeader("X-Real-IP"));

        if (StringUtils.isNotBlank(realIp)) {
            return realIp;
        }

        return request.getRemoteAddr();
    }

    @GetMapping({"time", "time/iso"})
    public String getTimeAsIsoString() {
        return Instant.now().toString();
    }

    @GetMapping("time/epoch")
    public long getTimeAsEpochMillis() {
        return Instant.now().toEpochMilli();
    }
}
