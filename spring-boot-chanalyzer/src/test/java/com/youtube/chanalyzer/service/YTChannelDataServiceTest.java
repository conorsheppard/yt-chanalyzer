package com.youtube.chanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class YTChannelDataServiceTest {

    public static MockWebServer mockPythonWebScraper;
    private YTChannelDataService ytChannelDataService;
    private final String VIDEOS_2 = "2_VIDEOS";
    private final String VIDEO_1 =  "1_VIDEO";
    private final String EMPTY = "EMPTY";

    @BeforeAll
    static void setUp() throws IOException {
        mockPythonWebScraper = new MockWebServer();
        mockPythonWebScraper.start();
    }

    @AfterAll
    static void tearDown() throws IOException {
        mockPythonWebScraper.shutdown();
    }

    class TestScraper implements ScraperAPI {
        @SneakyThrows
        @Override
        public Flux<?> getChannelVideoData(String responseFile) {
            String MOCK_RESPONSE_FILE = getResponseFile(responseFile);
            var responseBody = new Scanner(new File(MOCK_RESPONSE_FILE)).nextLine();
            TypeReference<List<HashMap<String, String>>> jacksonTypeReference = new TypeReference<>(){};
            List<HashMap<String, String>> jacksonList = new ObjectMapper().readValue(responseBody, jacksonTypeReference);

            return Flux.just(new ChartJSDataResponseDTO(jacksonList));
        }
    }

    private String getResponseFile(String responseFile) {
        return switch (responseFile) {
            case EMPTY -> "src/test/resources/service/mock-response-body-empty.txt";
            case VIDEO_1 -> "src/test/resources/service/mock-response-body-1-video.txt";
            case VIDEOS_2 -> "src/test/resources/service/mock-response-body-2-videos.txt";
            default -> throw new IllegalStateException("Unexpected value: " + responseFile);
        };
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

    @BeforeEach
    void initialize() {

        ytChannelDataService = new YTChannelDataService(new TestScraper());
    }


    @SneakyThrows
    @Test
    public void testGetChannelVideos() {
//        String MOCK_RESPONSE_FILE = "src/test/resources/service/mock-response-body.txt";
//        var responseBody = new Scanner(new File(MOCK_RESPONSE_FILE)).nextLine();
//        mockPythonWebScraper.enqueue(new MockResponse().setBody(responseBody)
//                .addHeader("Content-Type", "application/json"));
//        var channelUrl = "https://www.youtube.com/@NASA";
//        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(channelUrl);
//
//        StepVerifier.create(response)
//                .expectNextMatches(elem -> elem.getLabels().getFirst().contains("Dec"))
//                .expectComplete()
//                .verify();
//
//        RecordedRequest recordedRequest = mockPythonWebScraper.takeRequest();
//        assertEquals("GET", recordedRequest.getMethod());
    }

    @Test
    public void testScraper1VideoResponse() {
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(VIDEO_1);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().getFirst().contains("Dec"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testScraper2VideoResponse() {
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(VIDEOS_2);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().stream().toList().size() == 2)
                .expectComplete()
                .verify();
    }

    @Test
    public void testScraperEmptyResponse() {
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(EMPTY);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().isEmpty())
                .expectComplete()
                .verify();
    }
}
