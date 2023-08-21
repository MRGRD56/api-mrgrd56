package ru.mrgrd56.api.utils;

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public final class ValueStreamingResponseBody {
    private ValueStreamingResponseBody() { }

    public static StreamingResponseBody ofString(String value) {
        return ofString(value, StandardCharsets.UTF_8);
    }

    public static StreamingResponseBody ofString(String value, Charset charset) {
        return ofBytes(value.getBytes(charset));
    }

    public static StreamingResponseBody ofBytes(byte[] bytes) {
        return outputStream -> {
            outputStream.write(bytes);
            outputStream.close();
        };
    }
}
