package com.youtube.chanalyzer.scraper;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScraperAPITest {

    @InjectMocks
    private YouTubeChannelScraperAPI channelService;

    @Mock
    private WebClient webClient;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec requestHeadersSpec;
    private WebClient.ResponseSpec responseSpec;

    @BeforeEach
    void setUp() {
        requestHeadersUriSpec = mock(WebClient.RequestHeadersUriSpec.class);
        requestHeadersSpec = mock(WebClient.RequestHeadersSpec.class);
        responseSpec = mock(WebClient.ResponseSpec.class);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
    }

    @Test
    void getChannelVideoData_ShouldReturnExpectedData() {
        // Given
        String channelUrl = "https://example.com/channel";
        // Mock the WebClient chain
        setupWebClientMock();

        // When
        var result = channelService.getChannelVideoData(channelUrl, 100);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> {
                    // Add assertions based on your DTO structure
                    return dto.getLabels() != null;
                })
                .expectNextMatches(dto -> {
                    // Add assertions for second response
                    return dto.getLabels() != null;
                })
                .verifyComplete();
    }

    private ChartJSDataResponseDTO getChartResponseForTest(int interval) {
        return new ChartJSDataResponseDTO().setLabels(List.of()).setDatasets(List.of(Map.of("data", List.of()), Map.of("data", List.of()), Map.of("data", List.of()))).setCurrentInterval(interval);
    }

    @Test
    void getChannelVideoData_ShouldHandleError() {
        // Given
        String channelUrl = "https://example.com/channel";

        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(YouTubeVideoDTO.class))
                .thenReturn(Flux.error(new RuntimeException("WebClient error")));

        // When
        var result = channelService.getChannelVideoData(channelUrl, 100);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    private void setupWebClientMock() {
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.accept(any())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(YouTubeVideoDTO.class))
                .thenReturn(Flux.just(
                        getTestVideo("Title1", "https://youtube.com/watch?=1234", "12,000 views", "Dec 27, 2024"),
                        getTestVideo("Title1.1", "https://youtube.com/watch?=12345678", "100 views", "May 23, 2025")
                ))
                .thenReturn(Flux.just(getTestVideo("Title2", "https://youtube.com/watch?=5678", "1,000 views", "Mar 12, 2023")));
    }

    private static YouTubeVideoDTO getTestVideo(String title, String url, String views, String date) {
        return new YouTubeVideoDTO()
                .setTitle(title)
                .setUrl(url).
                setViews(views)
                .setPublishedTime(date);
    }
}
