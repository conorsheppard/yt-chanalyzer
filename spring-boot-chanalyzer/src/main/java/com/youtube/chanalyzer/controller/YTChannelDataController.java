package com.youtube.chanalyzer.controller;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.service.YTChannelDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
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
    private YTChannelDataService service;
    @Value("${YT_BASE_URL}")
    private String YT_BASE_URL;

    @GetMapping("/health")
    public ResponseEntity<Object> getHealthCheck() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new ResponseEntity<>(headers, HttpStatus.OK);
    }

    @GetMapping(path = "/v1/channel/{channelName}/videos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ChartJSDataResponseDTO> getChannelVideos(@PathVariable String channelName) {
        return service.getChannelVideoData(YT_BASE_URL + channelName);
    }
}
