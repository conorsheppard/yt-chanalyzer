package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.YTChannelDataResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class YTChannelDataService {
    private final WebClient webClient;
    List<Integer> currentUrlVideos = Arrays.asList(1, 2, 4, 8, 16, 24, 32, 48, 64, 88);

    @Autowired
    public YTChannelDataService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<YTChannelDataResponseDTO> initiateResponseStream(String channelUrl) {
        var fluxFromIterable = Flux
                .fromIterable(currentUrlVideos)
                .flatMap(i -> getScrapeResponse(i, channelUrl));

        fluxFromIterable.subscribe();

        return fluxFromIterable;
    }

    private Mono<YTChannelDataResponseDTO> getScrapeResponse(Integer numVideos, String channelUrl) {
        return webClient.get()
                .uri("?channelUrl=" + channelUrl + "&numVideos=" + numVideos)
                .retrieve()
                .bodyToMono(ArrayList.class)
                .map(YTChannelDataResponseDTO::new)
                .map(yt -> yt.setCurrentInterval(numVideos));
    }
}
