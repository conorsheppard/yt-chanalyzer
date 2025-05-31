package com.conorsheppard.app.controller;

import com.conorsheppard.app.entity.YouTubeVideo;
import com.conorsheppard.app.service.ScrapeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("/api")
public class ScrapeController {

    private final ScrapeService scrapeService;

    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @GetMapping(path = "/v1/channels", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<YouTubeVideo> scrapeYouTubeChannel(@RequestParam String channel,
                                                   @RequestParam(defaultValue = "100") int numVideos) {
//        var launched = new AtomicBoolean(false);
//        if (launched.getAndSet(true)) {
//            log.warn("Skipping launch; already launched.");
//            sink.complete();
//            return;
//        }
        return scrapeService.scrapeChannel(channel, numVideos);
    }
}