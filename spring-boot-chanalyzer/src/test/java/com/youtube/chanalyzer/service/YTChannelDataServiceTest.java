package com.youtube.chanalyzer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import com.youtube.chanalyzer.repo.ScrapeStatusRepository;
import com.youtube.chanalyzer.repo.ScrapedVideoRepository;
import com.youtube.chanalyzer.scraper.ScraperAPI;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.mock;


@ExtendWith(MockitoExtension.class)
class YTChannelDataServiceTest {

    private YTChannelDataService ytChannelDataService;
    private static final String VIDEOS_2 = "2_VIDEOS";
    private static final String VIDEO_1 = "1_VIDEO";
    private static final String EMPTY = "EMPTY";

    static class TestScraper implements ScraperAPI<YouTubeVideoDTO> {
        @SneakyThrows
        @Override
        public Flux<YouTubeVideoDTO> getChannelVideoData(String responseFile, int numVideos) {
            String mockResponseFile = getResponseFile(responseFile);
            List<YouTubeVideoDTO> videoList = new ObjectMapper()
                    .readValue(Path.of(mockResponseFile).toFile(), new TypeReference<>() {
                    });
            return Flux.fromIterable(videoList);
        }
    }

    private static String getResponseFile(String responseFile) {
        return switch (responseFile) {
            case EMPTY -> "src/test/resources/service/mock-response-body-empty.json";
            case VIDEO_1 -> "src/test/resources/service/mock-response-body-1-video.json";
            case VIDEOS_2 -> "src/test/resources/service/mock-response-body-2-videos.json";
            default -> throw new IllegalStateException("Unexpected value: " + responseFile);
        };
    }

    @BeforeEach
    void initialize() {
        ytChannelDataService = new YTChannelDataService(new TestScraper(), mock(ScrapedVideoRepository.class),
                mock(ScrapeStatusRepository.class));
    }

    @Test
    public void testScraper1VideoResponse() {
        Flux<YouTubeVideoDTO> response = ytChannelDataService.getChannelVideoData(VIDEO_1, 100);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.equals(new YouTubeVideoDTO("NASA 2025: To the Moon, Mars, and Beyond",
                        "https://youtube.com/videos?watch=PPQ29WRT-rU", "PPQ29WRT-rU", "269979", "Dec 27, 2024")))
                .expectComplete()
                .verify();
    }

    @Test
    public void testScraper2VideoResponse() {
        Flux<YouTubeVideoDTO> response = ytChannelDataService.getChannelVideoData(VIDEOS_2, 100);

        StepVerifier.create(response)
                .expectNextMatches(elem -> elem.getVideoId().equals("PPQ29WRT-rU"))
                .expectNextMatches(elem -> elem.getVideoId().equals("asdf"))
                .expectComplete()
                .verify();
    }

    @Test
    public void testScraperEmptyResponse() {
        Flux<YouTubeVideoDTO> response = ytChannelDataService.getChannelVideoData(EMPTY, 100);

        StepVerifier.create(response)
                .expectComplete()
                .verify();
    }
}
