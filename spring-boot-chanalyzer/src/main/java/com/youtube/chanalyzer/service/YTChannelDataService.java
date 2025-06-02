package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.scraper.ScraperAPI;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
public class YTChannelDataService {
    private final ScraperAPI<ChartJSDataResponseDTO> scraperAPI;

    @Autowired
    public YTChannelDataService(ScraperAPI<ChartJSDataResponseDTO> scraperAPI) {
        this.scraperAPI = scraperAPI;
    }

    public Flux<ChartJSDataResponseDTO> getChannelVideoData(String channelName, int numVideos) {
        return scraperAPI.getChannelVideoData(channelName, numVideos);
    }
}
