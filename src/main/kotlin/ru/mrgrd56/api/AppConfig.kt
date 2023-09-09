package ru.mrgrd56.api

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.javafaker.Faker
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.annotation.PreDestroy

@Configuration
class AppConfig {
    private val asyncHttpClient: AsyncHttpClient =
        Dsl.config()
            .setConnectTimeout(5000)
            .let { Dsl.asyncHttpClient(it) }

    @Bean
    fun asyncHttpClient() = asyncHttpClient

    @Bean
    fun objectMapper(): ObjectMapper = jacksonObjectMapper()
        .registerModule(JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    @PreDestroy
    fun closeClient() {
        asyncHttpClient.close()
    }

    @Bean
    fun faker() = Faker(Locale.US)
}