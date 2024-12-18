package com.youtube.chanalyzer.ytchanneldata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class YTChannelDataController {
    @Autowired
    private Environment env;
    Logger logger = LoggerFactory.getLogger(YTChannelDataController.class);
    WebClient client = WebClient.create();

    @GetMapping("/health")
    public ResponseEntity<YTChannelDataResponseDTO> getHealthCheck() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/channel")
    public ResponseEntity<YTChannelDataResponseDTO> getChannelVideos(@RequestParam String channelUrl,
                                                                     @RequestParam int numVideos) {
        logger.info("channelUrl: {}, numVideos: {}", channelUrl, numVideos);
        try {
            YTChannelDataResponseDTO response = getChannelAnalytics(channelUrl, numVideos);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private YTChannelDataResponseDTO getChannelAnalytics(String channelUrl, int numVideos) {
        YTChannelDataResponseDTO response = new YTChannelDataResponseDTO();
        Map<String, Integer> monthsAndNumUploadsMap = new HashMap<>();

        List<String> datasets = new ArrayList<>();
        ArrayList<HashMap> responseBody = client.get()
                .uri(env.getProperty("scraper_api") + "/scrape?channelUrl=" + channelUrl + "&numVideos=" + numVideos)
                .retrieve()
                .bodyToMono(ArrayList.class)
                .block();

        for (HashMap<String, String> res : responseBody) {
            String videoDate = res.get("uploadDate");
            Pattern pattern = Pattern.compile("( \\d{1,2},)");
            Matcher matcher = pattern.matcher(videoDate);
            matcher.find();
            matcher.group(0);
            String currentMonthAndYear = videoDate.replace(matcher.group(0), ",");
            var currentMonthValue = monthsAndNumUploadsMap.get(currentMonthAndYear) == null ? 0 : monthsAndNumUploadsMap.get(currentMonthAndYear);
            monthsAndNumUploadsMap.put(currentMonthAndYear, currentMonthValue + 1);
        }
        List<String> labels = new ArrayList<>(monthsAndNumUploadsMap.keySet());
        response.setLabels(labels);
        for (Map.Entry<String, Integer> entry : monthsAndNumUploadsMap.entrySet()) {
            datasets.add(entry.getValue().toString());
        }
        response.setDatasets(datasets);

        return response;
    }
}
