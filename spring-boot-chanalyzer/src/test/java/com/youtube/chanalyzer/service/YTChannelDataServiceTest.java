package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.YTChannelDataResponseDTO;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class YTChannelDataServiceTest {

    public static MockWebServer mockPythonWebScraper;
    private YTChannelDataService ytChannelDataService;
    private final String MOCK_RESPONSE_FILE = "src/test/resources/service/mock-response-body.txt";

    @BeforeAll
    static void setUp() throws IOException {
        mockPythonWebScraper = new MockWebServer();
        mockPythonWebScraper.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockPythonWebScraper.shutdown();
    }

    @BeforeEach
    void initialize() {
        String baseUrl = String.format("http://localhost:%s", mockPythonWebScraper.getPort());
        ytChannelDataService = new YTChannelDataService(WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build());
    }


    @Test
    public void initiateResponseStreamTest() throws InterruptedException, FileNotFoundException {
        var responseBody = new Scanner(new File(MOCK_RESPONSE_FILE)).nextLine();
        mockPythonWebScraper.enqueue(new MockResponse().setBody(responseBody)
                .addHeader("Content-Type", "application/json"));
        var channelUrl = "https://www.youtube.com/@NASA";
        Flux<YTChannelDataResponseDTO> response = ytChannelDataService.initiateResponseStream(channelUrl);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().contains("Dec"))
                .verifyComplete();

        RecordedRequest recordedRequest = mockPythonWebScraper.takeRequest();
        assertEquals("GET", recordedRequest.getMethod());
    }

}
