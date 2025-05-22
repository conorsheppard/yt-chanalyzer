package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataServiceMockTest {

    public static MockWebServer mockPythonWebScraper;
    private YTChannelDataService ytChannelDataService;

    @BeforeAll
    static void setUp() throws IOException {
        mockPythonWebScraper = new MockWebServer();
        mockPythonWebScraper.start();
    }

    @BeforeEach
    void initialize() {
        ytChannelDataService = new YTChannelDataService(new TestScraperMock());
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockPythonWebScraper.shutdown();
    }

    static class TestScraperMock implements ScraperAPI {
        String baseUrl = String.format("http://localhost:%s", mockPythonWebScraper.getPort());
        private final WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();

        @Override
        public Flux<?> getChannelVideoData(String url) {
            return Flux
                    .fromIterable(List.of(1))
                    .flatMap(i -> wc.get()
                            .retrieve()
                            .bodyToMono(ArrayList.class)
                            .map(ChartJSDataResponseDTO::new)
                            .map(yt -> yt.setCurrentInterval(i)));
        }
    }

    @SneakyThrows
    @Test
    void testGetChannelVideos() {
        String MOCK_RESPONSE_FILE = "src/test/resources/service/mock-response-body-1-video.txt";
        var responseBody = new Scanner(new File(MOCK_RESPONSE_FILE)).nextLine();
        mockPythonWebScraper.enqueue(new MockResponse().setBody(responseBody)
                .addHeader("Content-Type", "application/json"));
        var channelUrl = "https://www.youtube.com/@NASA";
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(channelUrl);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().getFirst().contains("Dec"))
                .expectComplete()
                .verify();

        RecordedRequest recordedRequest = mockPythonWebScraper.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }
}
