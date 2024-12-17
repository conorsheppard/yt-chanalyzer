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
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class YTChannelDataController {
    @Autowired
    private Environment env;
    Logger logger = LoggerFactory.getLogger(YTChannelDataController.class);
    List<String> labels = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
    List<String> monthsShort = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
    WebClient client = WebClient.create();

    @GetMapping("/health")
    public ResponseEntity<YTChannelDataResponseDTO> getHealthCheck() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping("/channel")
    public ResponseEntity<YTChannelDataResponseDTO> getChannelVideos(@RequestParam(required = true) String channelUrl) {
        logger.info(channelUrl);
        try {
            YTChannelDataResponseDTO response = getChannelAnalytics(channelUrl);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private YTChannelDataResponseDTO getChannelAnalytics(String channelUrl) {
        YTChannelDataResponseDTO response = new YTChannelDataResponseDTO();
        Map<String, Integer> monthsAndNumUploadsMap = new HashMap<>() {{
            put("Jan", 0);
            put("Feb", 0);
            put("Mar", 0);
            put("Apr", 0);
            put("May", 0);
            put("Jun", 0);
            put("Jul", 0);
            put("Aug", 0);
            put("Sep", 0);
            put("Oct", 0);
            put("Nov", 0);
            put("Dec", 0);
        }};

        List<String> datasets = new ArrayList<>();

        logger.info("api: {}", env.getProperty("scraper_api"));

        logger.info("Before request to scraper API");
        ArrayList<HashMap> responseBody = client.get()
                .uri(env.getProperty("scraper_api") + "/scrape?channelUrl=" + channelUrl)
                .retrieve()
                .bodyToMono(ArrayList.class)
                .block();
        logger.info("After request to scraper API");

        System.out.println(responseBody);

        for (HashMap<String, String> res : responseBody) {
//            String viewsParsed = res.get("viewCount").substring(0, res.get("viewCount").length() - 6).replace(",", "");
            String videoDate = res.get("uploadDate");
            String currentMonth = monthsShort.stream().filter(videoDate::contains).toList().getFirst();
            monthsAndNumUploadsMap.put(currentMonth, monthsAndNumUploadsMap.get(currentMonth) + 1);
        }
        response.setLabels(labels);
        for (Map.Entry<String, Integer> entry : monthsAndNumUploadsMap.entrySet()) {
            datasets.add(entry.getValue().toString());
        }
        response.setDatasets(datasets);

        return response;
    }
}
