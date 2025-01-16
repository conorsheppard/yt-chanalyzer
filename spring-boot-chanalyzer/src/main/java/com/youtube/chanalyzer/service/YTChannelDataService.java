package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.scraper.YouTubeChannelScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class YTChannelDataService {
    private final YouTubeChannelScraperAPI scraperAPI;

    @Autowired
    public YTChannelDataService(YouTubeChannelScraperAPI scraperAPI) {
        this.scraperAPI = scraperAPI;
    }

    public Flux<ChartJSDataResponseDTO> getChannelVideoData(String channelUrl) {
        return scraperAPI.getChannelVideoData(channelUrl);
    }
}
