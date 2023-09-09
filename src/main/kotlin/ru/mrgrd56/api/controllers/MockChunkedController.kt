package ru.mrgrd56.api.controllers

import com.github.javafaker.Faker
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.PrintStream

@RestController
@RequestMapping("mock/chunked")
class MockChunkedController(
    private val faker: Faker
) {
    @GetMapping(produces = [MediaType.TEXT_PLAIN_VALUE])
    suspend fun getChunkedData(
        @RequestParam(defaultValue = 20.toString()) count: Int,
        @RequestParam(defaultValue = 300.toString()) delay: Long
    ): ResponseEntity<StreamingResponseBody> = coroutineScope {
        val responseBody = StreamingResponseBody { outputStream ->
            runBlocking {
                val printStream = PrintStream(outputStream)
                repeat(count) {
                    delay(delay)
                    printStream.println(faker.name().fullName())
                    printStream.flush()
                    outputStream.flush()
                }
            }
        }

        ResponseEntity.ok(responseBody)
    }
}