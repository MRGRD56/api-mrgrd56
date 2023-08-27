package ru.mrgrd56.api

import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.Dsl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.annotation.PreDestroy

@Configuration
class AppConfig {
    private val asyncHttpClient: AsyncHttpClient =
        Dsl.config()
            .setConnectTimeout(5000)
            .let { Dsl.asyncHttpClient(it) }

    @Bean
    fun asyncHttpClient() = asyncHttpClient

    @PreDestroy
    fun closeClient() {
        asyncHttpClient.close()
    }
}