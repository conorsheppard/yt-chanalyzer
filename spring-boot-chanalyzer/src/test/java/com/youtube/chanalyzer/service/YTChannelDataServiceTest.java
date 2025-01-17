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
        Flux flux = Flux.just(
                new ChartJSDataResponseDTO(new ArrayList<>(
                        List.of(new HashMap<>(Map.of(
                                        "videoId", "PPQ29WRT-rU",
                                        "title", "NASA 2025: To the Moon, Mars, and Beyond",
                                        "uploadDate", "Dec 27, 2024",
                                        "viewCount", "269,979 views")),
                                new HashMap<>(Map.of(
                                        "videoId", "PPQ29WRT-rU",
                                        "title", "NASA 2025: To the Moon, Mars, and Beyond",
                                        "uploadDate", "Dec 27, 2024",
                                        "viewCount", "269,979 views"))
                        ))));

        return flux;
        }
    }

    private String getResponseFile(String responseFile) {
        return switch (responseFile) {
            case "EMPTY" -> "src/test/resources/service/mock-response-body-empty.txt";
            case "1_VIDEO" -> "src/test/resources/service/mock-response-body-1-video.txt";
            case "2_VIDEO" -> "src/test/resources/service/mock-response-body-2-videos.txt";
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
        var responseFile = "1_VIDEO";
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(responseFile);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().getFirst().contains("Dec"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testScraper2VideoResponse() {
        var responseFile = "2_VIDEOS";
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(responseFile);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().stream().filter(i -> i.contains("Dec")).toList().size() == 2)
                .expectComplete()
                .verify();
    }

    @Test
    public void testScraperEmptyResponse() {
        var responseFile = "EMPTY";
        Flux<ChartJSDataResponseDTO> response = ytChannelDataService.getChannelVideoData(responseFile);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getLabels().isEmpty())
                .expectComplete()
                .verify();
    }
}
