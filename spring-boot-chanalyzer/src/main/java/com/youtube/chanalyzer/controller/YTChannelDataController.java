package com.youtube.chanalyzer.controller;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.service.YTChannelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class YTChannelDataController {
    @Autowired
    YTChannelDataService service;

    @GetMapping("/health")
    public ResponseEntity<Object> getHealthCheck() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping(path = "/v1/channel/{channelUrl}/videos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChartJSDataResponseDTO> getChannelVideos(@PathVariable String channelUrl) {
        return service.getChannelVideoData(channelUrl);
    }
}
