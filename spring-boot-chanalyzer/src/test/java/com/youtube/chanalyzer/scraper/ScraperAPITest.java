package com.youtube.chanalyzer.scraper;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ScraperAPITest {

    @InjectMocks
    private YouTubeChannelScraperAPI channelService;

    @Mock
    private WebClient webClient;

    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;
    private WebClient.RequestHeadersSpec<?> requestHeadersSpec;
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
        ArrayList<Object> responseData1 = new ArrayList<>(); // Add test data
        ArrayList<Object> responseData2 = new ArrayList<>(); // Add test data

        // Mock the WebClient chain
        setupWebClientMock(responseData1, responseData2);

        // When
        var result = channelService.getChannelVideoData(channelUrl, 100);

        // Then
        StepVerifier.create(result)
                .expectNextMatches(dto -> {
                    // Add assertions based on your DTO structure
                    return dto.getCurrentInterval() != null;
                })
                .expectNextMatches(dto -> {
                    // Add assertions for second response
                    return dto.getCurrentInterval() != null;
                })
                .expectNext(getChartResponseForTest(4))
                .expectNext(getChartResponseForTest(8))
                .expectNext(getChartResponseForTest(16))
                .expectNext(getChartResponseForTest(24))
                .expectNext(getChartResponseForTest(32))
                .expectNext(getChartResponseForTest(48))
                .expectNext(getChartResponseForTest(64))
                .expectNext(getChartResponseForTest(88))
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
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ArrayList.class))
                .thenReturn(Mono.error(new RuntimeException("WebClient error")));

        // When
        var result = channelService.getChannelVideoData(channelUrl, 100);

        // Then
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    private void setupWebClientMock(ArrayList<Object> responseData1, ArrayList<Object> responseData2) {
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ArrayList.class))
                .thenReturn(Mono.just(responseData1))
                .thenReturn(Mono.just(responseData2));
    }
}
