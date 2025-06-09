package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import com.youtube.chanalyzer.entity.ScrapeStatus;
import com.youtube.chanalyzer.entity.ScrapedVideo;
import com.youtube.chanalyzer.repo.ScrapeStatusRepository;
import com.youtube.chanalyzer.repo.ScrapedVideoRepository;
import com.youtube.chanalyzer.scraper.ScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.youtube.chanalyzer.scraper.YouTubeChannelScraperAPI.parseViewCount;

@Service
public class YTChannelDataService {
    private final ScraperAPI<YouTubeVideoDTO> scraperAPI;
    private final ScrapedVideoRepository scrapedVideoRepository;
    private final ScrapeStatusRepository scrapeStatusRepository;
    private static final String COMPLETED = "completed";
    private static final String IN_PROGRESS = "in_progress";
    private static final String FAILED = "failed";

    @Autowired
    public YTChannelDataService(ScraperAPI<YouTubeVideoDTO> scraperAPI,
                                ScrapedVideoRepository scrapedVideoRepository,
                                ScrapeStatusRepository scrapeStatusRepository) {
        this.scraperAPI = scraperAPI;
        this.scrapedVideoRepository = scrapedVideoRepository;
        this.scrapeStatusRepository = scrapeStatusRepository;
    }

    public Flux<YouTubeVideoDTO> getChannelVideoData(String channelName, int numVideos) {
        LocalDate today = LocalDate.now();

        Optional<ScrapeStatus> existingStatus = scrapeStatusRepository.findByChannelNameAndScrapeDate(channelName, today);

        if (existingStatus.isPresent()) {
            switch (existingStatus.get().getStatus()) {
                case COMPLETED:
                    return getCachedData(channelName, today);
                case IN_PROGRESS:
                    return waitForScrapeToComplete(channelName, today);
                case FAILED:
                    break;
            }
        }

        // Mark as in progress
        ScrapeStatus status = new ScrapeStatus();
        status.setChannelName(channelName);
        status.setScrapeDate(today);
        status.setStatus(IN_PROGRESS);
        status.setStartedAt(LocalDateTime.now());
        scrapeStatusRepository.save(status);

        return scraperAPI.getChannelVideoData(channelName, numVideos)
                .map(data -> {
                    ScrapedVideo video = new ScrapedVideo();
                    video.setChannelName(channelName);
                    video.setVideoTitle(data.getTitle());
                    video.setVideoId(data.getVideoId());
                    video.setPublishedDate(LocalDate.parse(normalizeMonth(data.getPublishedTime()),
                            DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH)));
                    video.setViews((int) parseViewCount(data.getViews()));
                    video.setMonthLabel(data.getPublishedTime());
                    video.setScrapedDate(today);
                    scrapedVideoRepository.save(video);

                    data.setViews(parseViewCount(data.getViews()) + "");
                    return data;
                })
                .doOnComplete(() -> {
                    status.setStatus(COMPLETED);
                    status.setUpdatedAt(LocalDateTime.now());
                    scrapeStatusRepository.save(status);
                })
                .doOnError(err -> {
                    status.setStatus(FAILED);
                    status.setUpdatedAt(LocalDateTime.now());
                    scrapeStatusRepository.save(status);
                });
    }

    private Flux<YouTubeVideoDTO> getCachedData(String channelName, LocalDate date) {
        List<ScrapedVideo> cached = scrapedVideoRepository.findByChannelNameAndScrapedDate(channelName, date);
        return Flux.fromIterable(cached.stream().map(v ->
                        new YouTubeVideoDTO()
                                .setViews(v.getViews() + "")
                                .setPublishedTime(v.getPublishedDate().toString()))
                .toList());
    }

    private Flux<YouTubeVideoDTO> waitForScrapeToComplete(String channelName, LocalDate date) {
        return Flux.defer(() -> {
            for (int i = 0; i < 10; i++) {
                Optional<ScrapeStatus> statusOpt = scrapeStatusRepository.findByChannelNameAndScrapeDate(channelName, date);
                if (statusOpt.isPresent() && COMPLETED.equals(statusOpt.get().getStatus())) {
                    return getCachedData(channelName, date);
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
            return Flux.error(new IllegalStateException("Scrape still in progress or failed"));
        });
    }

    private static String normalizeMonth(String dateStr) {
        return dateStr.replace("Sept", "Sep");
    }
}
