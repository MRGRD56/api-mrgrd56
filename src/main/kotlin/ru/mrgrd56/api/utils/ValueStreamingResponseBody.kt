package ru.mrgrd56.api.utils

import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.OutputStream
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

object ValueStreamingResponseBody {
    @JvmStatic
    @JvmOverloads
    fun ofString(value: String, charset: Charset = StandardCharsets.UTF_8): StreamingResponseBody {
        return ofBytes(value.toByteArray(charset))
    }

    @JvmStatic
    fun ofBytes(bytes: ByteArray): StreamingResponseBody {
        return StreamingResponseBody { outputStream: OutputStream ->
            outputStream.write(bytes)
            outputStream.close()
        }
    }
}
