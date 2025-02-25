package com.youtube.chanalyzer.scraper;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class YouTubeChannelScraperAPITest {

    public static MockWebServer mockPythonWebScraper;

    final Dispatcher dispatcher = new Dispatcher() {
        @NotNull
        @Override
        public MockResponse dispatch(@NotNull RecordedRequest request) {
            return new MockResponse().setResponseCode(200).setBody("[{\"videoId\": \"PPQ29WRT-rU\", \"title\": \"NASA 2025: To the Moon, Mars, and Beyond\", \"uploadDate\": \"Dec 27, 2024\", \"viewCount\": \"269,979 views\"}]");
        }
    };

    @BeforeAll
    static void setUp() throws IOException {
        mockPythonWebScraper = new MockWebServer();
        mockPythonWebScraper.start();
    }

    @Test
    public void getChannelVideoData() {
        String baseUrl = String.format("http://localhost:%s", mockPythonWebScraper.getPort());
        mockPythonWebScraper.setDispatcher(dispatcher);
//        mockPythonWebScraper.enqueue(new MockResponse().setBody("[{\"videoId\": \"PPQ29WRT-rU\", \"title\": \"NASA 2025: To the Moon, Mars, and Beyond\", \"uploadDate\": \"Dec 27, 2024\", \"viewCount\": \"269,979 views\"}]"));
        final WebClient wc = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_EVENT_STREAM_VALUE)
                .build();
        var scraperAPI = new YouTubeChannelScraperAPI(wc);
        var response = scraperAPI.getChannelVideoData(baseUrl);

        List<ChartJSDataResponseDTO> elements = new ArrayList<>();

        response.subscribe(elements::add);

        assertNull(elements.getFirst());
    }
}
