package com.conorsheppard.app.controller;

import com.conorsheppard.app.entity.YouTubeVideo;
import com.conorsheppard.app.service.ScrapeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class ScrapeController {

    private final ScrapeService scrapeService;

    public ScrapeController(ScrapeService scrapeService) {
        this.scrapeService = scrapeService;
    }

    @GetMapping("/v1/channels")
    public Flux<YouTubeVideo> scrapeYouTubeChannel(
            @RequestParam String channelName,
            @RequestParam(defaultValue = "100") int limit) {
        return scrapeService.scrapeChannel(channelName, limit);
    }
}