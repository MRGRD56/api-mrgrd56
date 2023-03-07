package ru.mrgrd56.api.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.time.Instant;

@RestController
public class HomeController {
    @GetMapping("ip")
    public String getIpAddress(HttpServletRequest request) {
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
