package com.youtube.chanalyzer.scraper;

import reactor.core.publisher.Flux;

public interface ScraperAPI {
    Flux<?> getChannelVideoData(String url);
}
