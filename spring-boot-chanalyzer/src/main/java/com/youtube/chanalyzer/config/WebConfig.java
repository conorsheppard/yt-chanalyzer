package com.youtube.chanalyzer.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Configuration
public class WebConfig {
    @Autowired
    private Environment env;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(Objects.requireNonNull(env.getProperty("scraper_api")))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();
    }
}
