package com.youtube.chanalyzer.scraper;

import reactor.core.publisher.Flux;

public interface ScraperAPI<E> {
    Flux<E> getChannelVideoData(String channelName, int numVideos);
}
