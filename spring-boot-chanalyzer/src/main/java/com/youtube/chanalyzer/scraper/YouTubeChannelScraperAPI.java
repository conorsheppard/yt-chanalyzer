package com.youtube.chanalyzer.scraper;

import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.PrematureCloseException;

@AllArgsConstructor
@Slf4j
@Component
public class YouTubeChannelScraperAPI implements ScraperAPI<YouTubeVideoDTO> {
    private final WebClient webClient;

    public Flux<YouTubeVideoDTO> getChannelVideoData(String channelName, int numVideos) {
        return getScrapeResponse(channelName, numVideos);
    }

    private Flux<YouTubeVideoDTO> getScrapeResponse(String channelName, int numVideos) {
        return webClient.get()
                .uri("?channel=" + channelName + "&numVideos=" + numVideos)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .retrieve()
                .bodyToFlux(YouTubeVideoDTO.class)
                .log()
                .share()
                .onErrorResume(WebClientResponseException.class, ex -> {
                    // Handles HTTP status-related errors (e.g. 4xx, 5xx or malformed response)
                    log.error("WebClientResponseException: {}", ex.getMessage(), ex);
                    return Flux.error(new RuntimeException("Scrape failed due to unexpected response"));
                })
                .onErrorResume(PrematureCloseException.class, ex -> {
                    // Handles connection dropped mid-response
                    log.error("PrematureCloseException: {}", ex.getMessage(), ex);
                    return Mono.error(new RuntimeException("Scraper backend closed connection early"));
                })
                .onErrorResume(Throwable.class, ex -> {
                    // Catch-all fallback
                    log.error("Unexpected error during scrape: {}", ex.getMessage(), ex);
                    return Mono.error(new RuntimeException("Unexpected scrape error"));
                });
    }

    public static int parseViewCount(String viewCountText) {
        if (viewCountText == null || viewCountText.isBlank()) return 0;

        String cleaned = viewCountText
                .toUpperCase()
                .replace(" VIEWS", "")
                .replace("PREMIERED ", "")
                .replace(",", "")
                .trim();

        int multiplier = 1;

        if (cleaned.endsWith("K")) {
            multiplier = 1_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        } else if (cleaned.endsWith("M")) {
            multiplier = 1_000_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        } else if (cleaned.endsWith("B")) {
            multiplier = 1_000_000_000;
            cleaned = cleaned.substring(0, cleaned.length() - 1);
        }

        try {
            return Integer.parseInt(cleaned) * multiplier;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
