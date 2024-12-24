package com.youtube.chanalyzer.ytchanneldata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class YTChannelDataController {
    @Autowired
    private Environment env;
    private final Logger logger = LoggerFactory.getLogger(YTChannelDataController.class);
    private final WebClient client = WebClient.create();
    private final int[] videoQuantityIntervals = new int[]{1, 2, 4, 8, 16, 24, 32, 48, 64, 88};

    class ChannelAndNumVideos {
        String channelUrl;
        int numVideos;

        ChannelAndNumVideos(int numVideos) {
            this.numVideos = numVideos;
        }

        public ChannelAndNumVideos setChannelUrl(String channelUrl) {
            this.channelUrl = channelUrl;
            return this;
        }

        String getChannelUrl() {
            return channelUrl;
        }

        int getNumVideos() {
            return numVideos;
        }
    }

    @GetMapping("/health")
    public ResponseEntity getHealthCheck() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping(path = "/channel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<YTChannelDataResponseDTO> getChannelVideos(@RequestParam String channelUrl) {
        var currentUrlVideos = Arrays
                .stream(videoQuantityIntervals)
                .mapToObj(ChannelAndNumVideos::new)
                .map(c -> c.setChannelUrl(channelUrl))
                .collect(Collectors.toList());

        var fluxFromIterable = Flux
                .fromIterable(currentUrlVideos)
                .flatMap(this::getScrapeResponse);

        fluxFromIterable.subscribe();

        return fluxFromIterable;
    }

    private Mono<YTChannelDataResponseDTO> getScrapeResponse(ChannelAndNumVideos channelAndNumVideos) {
        return client.get()
                .uri(env.getProperty("scraper_api") + "?channelUrl=" + channelAndNumVideos.getChannelUrl() + "&numVideos=" + channelAndNumVideos.getNumVideos())
                .retrieve()
                .bodyToMono(ArrayList.class)
                .map(YTChannelDataResponseDTO::new)
                .map(yt -> yt.setCurrentInterval(channelAndNumVideos.getNumVideos()));
    }
}
