package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import com.youtube.chanalyzer.repo.ScrapeStatusRepository;
import com.youtube.chanalyzer.repo.ScrapedVideoRepository;
import com.youtube.chanalyzer.scraper.ScraperAPI;
import lombok.SneakyThrows;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

public class DataServiceMockTest {

    public static MockWebServer mockPlaywrightWebScraper;
    private YTChannelDataService ytChannelDataService;
    private static ScrapedVideoRepository scrapedVideoRepository;
    private static ScrapeStatusRepository scrapeStatusRepository;

    @BeforeAll
    static void setUp() throws IOException {
        mockPlaywrightWebScraper = new MockWebServer();
        mockPlaywrightWebScraper.start();
        scrapedVideoRepository = mock(ScrapedVideoRepository.class);
        scrapeStatusRepository = mock(ScrapeStatusRepository.class);
    }

    @BeforeEach
    void initialize() {
        ytChannelDataService = new YTChannelDataService(new TestScraperMock(), scrapedVideoRepository,
                scrapeStatusRepository);
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockPlaywrightWebScraper.shutdown();
    }

    static class TestScraperMock implements ScraperAPI<YouTubeVideoDTO> {
        String baseUrl = String.format("http://localhost:%s", mockPlaywrightWebScraper.getPort());
        private final WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();

        @Override
        public Flux<YouTubeVideoDTO> getChannelVideoData(String url, int numVideos) {
            return wc.get().uri("?channel=" + url + "&numVideos=" + numVideos)
                    .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                    .retrieve()
                    .bodyToFlux(YouTubeVideoDTO.class);
        }
    }

    @SneakyThrows
    @Test
    void testGetChannelVideos() {
        String mockResponseFile = "src/test/resources/service/mock-response-data-service-test.json";
        var responseBody = new Scanner(new File(mockResponseFile)).nextLine();
        mockPlaywrightWebScraper.enqueue(new MockResponse().setBody(responseBody)
                .addHeader("Content-Type", "application/json"));
        Flux<YouTubeVideoDTO> response = ytChannelDataService.getChannelVideoData("https://www.youtube.com/@NASA", 100);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.equals(new YouTubeVideoDTO("NASA 2025: To the Moon, Mars, and Beyond",
                        "https://youtube.com/videos?watch=PPQ29WRT-rU", "PPQ29WRT-rU", "269979", "Dec 27, 2024")))
                .expectComplete()
                .verify();

        RecordedRequest recordedRequest = mockPlaywrightWebScraper.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }
}
