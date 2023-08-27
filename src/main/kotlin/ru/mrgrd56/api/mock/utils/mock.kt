package ru.mrgrd56.api.mock.utils

import kotlinx.coroutines.delay
import org.springframework.http.ResponseEntity
import ru.mrgrd56.api.mock.model.MockOptions

suspend fun <T> mock(options: MockOptions, action: () -> ResponseEntity<T>): ResponseEntity<T> {
    if (options.isWithDelay) {
        delay((250..500).random().toLong())
    }

    return action()
}