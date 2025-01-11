package com.youtube.chanalyzer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Objects;

@Configuration
public class WebConfig {
    @Value("${SCRAPER_API}")
    private String SCRAPER_API;
    @Value("${SCRAPER_URL_LOCAL}")
    private String SCRAPER_URL_LOCAL;

    @Bean
    public WebClient webClient() {
        var scraperURL = Objects.isNull(SCRAPER_URL_LOCAL) ? SCRAPER_API : SCRAPER_URL_LOCAL;
        return WebClient.builder()
                .baseUrl(Objects.requireNonNull(scraperURL))
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();
    }
}
