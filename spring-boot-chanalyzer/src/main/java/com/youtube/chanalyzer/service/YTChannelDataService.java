package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import com.youtube.chanalyzer.entity.ScrapedVideo;
import com.youtube.chanalyzer.repo.ScrapedVideoRepository;
import com.youtube.chanalyzer.scraper.ScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.youtube.chanalyzer.scraper.YouTubeChannelScraperAPI.parseViewCount;

@Service
public class YTChannelDataService {
    private final ScraperAPI<YouTubeVideoDTO> scraperAPI;
    private final ScrapedVideoRepository scrapedVideoRepository;

    @Autowired
    public YTChannelDataService(ScraperAPI<YouTubeVideoDTO> scraperAPI, ScrapedVideoRepository scrapedVideoRepository) {
        this.scraperAPI = scraperAPI;
        this.scrapedVideoRepository = scrapedVideoRepository;
    }

    public Flux<YouTubeVideoDTO> getChannelVideoData(String channelName, int numVideos) {
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
                    scrapedVideoRepository.save(video);

                    data.setViews(parseViewCount(data.getViews()) + "");
                    return data;
                });
    }

    private static String normalizeMonth(String dateStr) {
        return dateStr.replace("Sept", "Sep");
    }
}
