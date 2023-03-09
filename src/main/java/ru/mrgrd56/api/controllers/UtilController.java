package ru.mrgrd56.api.controllers;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("util")
public class UtilController {
    @RequestMapping(value = "delay", method = {RequestMethod.GET, RequestMethod.POST})
    public String delay(@RequestParam long ms) throws InterruptedException {
        Thread.sleep(ms);
        return Objects.toString(Math.random());
    }

    @RequestMapping(value = "http-response", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> getHttpResponse(
            @RequestParam(required = false, defaultValue = "200") Integer status,
            @RequestParam(required = false) List<String> header,
            @RequestParam(required = false) Object body) {
        HttpStatus responseStatus = status == null ? HttpStatus.OK : HttpStatus.valueOf(status);
        HttpHeaders responseHeaders = new HttpHeaders();

        if (header != null) {
            for (var rawHeader : header) {
                String[] parts = rawHeader.split(":\\n?", 2);
                if (parts.length != 2) {
                    return ResponseEntity.badRequest().body("Invalid header '" + rawHeader + "'");
                }

                responseHeaders.add(ObjectUtils.requireNonEmpty(parts[0]), parts[1]);
            }
        }

        return new ResponseEntity<>(body, responseHeaders, responseStatus);
    }
}
