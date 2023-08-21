package ru.mrgrd56.api;

import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.Dsl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PreDestroy;
import java.io.IOException;

@Configuration
public class AppConfig {
    private final AsyncHttpClient asyncHttpClient = Dsl.asyncHttpClient(
            Dsl.config()
                    .setConnectTimeout(5000)
    );

    @Bean
    public AsyncHttpClient asyncHttpClient() {
        return asyncHttpClient;
    }

    @PreDestroy
    public void closeClient() throws IOException {
        asyncHttpClient.close();
    }
}
