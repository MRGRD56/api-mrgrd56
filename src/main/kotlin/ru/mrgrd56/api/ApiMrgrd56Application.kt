package ru.mrgrd56.api

import nu.pattern.OpenCV
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ApiMrgrd56Application {
    init {
        OpenCV.loadLocally()
    }
}

fun main(args: Array<String>) {
    runApplication<ApiMrgrd56Application>(*args)
}