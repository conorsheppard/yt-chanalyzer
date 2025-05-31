package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.scraper.ScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class YTChannelDataService {
    private final ScraperAPI scraperAPI;

    @Autowired
    public YTChannelDataService(ScraperAPI scraperAPI) {
        this.scraperAPI = scraperAPI;
    }

    @SuppressWarnings("unchecked")
    public Flux<ChartJSDataResponseDTO> getChannelVideoData(String channelName, int numVideos) {
        return (Flux<ChartJSDataResponseDTO>) scraperAPI.getChannelVideoData(channelName, numVideos);
    }
}
