package ru.mrgrd56.api.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.mrgrd56.api.proxy.ProxyService;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("proxy")
public class ProxyController {
    private final ProxyService proxyService;

    public ProxyController(ProxyService proxyService) {
        this.proxyService = proxyService;
    }

    @RequestMapping
    public ResponseEntity<StreamingResponseBody> proxy(
            @RequestParam String url,
            @RequestHeader org.springframework.http.HttpHeaders requestHeadersIn,
            HttpServletRequest requestIn) {
        return proxyService.proxyRequest(url, requestHeadersIn, requestIn);
    }
}
