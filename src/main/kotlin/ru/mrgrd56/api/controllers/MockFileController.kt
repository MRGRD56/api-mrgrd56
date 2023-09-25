package ru.mrgrd56.api.controllers

import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody

const val BUFFER_SIZE = 8 * 1024

@RestController
@RequestMapping("mock/file")
class MockFileController {
    @GetMapping("generated")
    fun downloadGeneratedFile(@RequestParam size: Long): ResponseEntity<StreamingResponseBody> {
        val buffer = ByteArray(BUFFER_SIZE) { 0xff.toByte() }

        return StreamingResponseBody {
            it.use { outputStream ->
                var bytesRemaining = size

                while (bytesRemaining > 0) {
                    val bytesToWrite = BUFFER_SIZE.toLong().coerceAtMost(bytesRemaining).toInt()

                    outputStream.write(buffer, 0, bytesToWrite)
                    bytesRemaining -= bytesToWrite
                }
            }
        }.let {
            ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(size)
                .header("Content-Disposition", "attachment; filename=\"${System.currentTimeMillis()}.bin\"")
                .body(it)
        }
    }
}