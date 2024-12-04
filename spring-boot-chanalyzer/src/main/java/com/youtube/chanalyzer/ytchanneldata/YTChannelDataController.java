package com.youtube.chanalyzer.ytchanneldata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class YTChannelDataController {
    Logger logger = LoggerFactory.getLogger(YTChannelDataController.class);
    List<String> labels = Arrays.asList("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");

    @GetMapping("/channel")
    public ResponseEntity<YTChannelDataResponseDTO> getChannelVideos(@RequestParam(required = true) String channelId) {
        logger.info(channelId);
        try {
            YTChannelDataResponseDTO response = scrape(channelId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private YTChannelDataResponseDTO scrape(String channelId) {
        logger.info("Working Directory: {}", System.getProperty("user.dir"));
        ProcessBuilder ps = new ProcessBuilder("python3", "get_yt_channel_videos.py", channelId);
        YTChannelDataResponseDTO response = new YTChannelDataResponseDTO();
        Map<String, Integer> monthsAndVideoViewsMap = new HashMap<>() {{
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

        try {
            logger.info("starting process ...");
            Process pr = ps.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            String videoID;
            List<String> datasets = new ArrayList<>();
            while ((videoID = in.readLine()) != null) {
                logger.info("video ID: {}", videoID);
                String videoViewsStr = in.readLine();
                logger.info("video views: {}", videoViewsStr);
                String viewsParsed = videoViewsStr.substring(0, videoViewsStr.length() - 6).replace(",", "");
                int viewsAsInt = Integer.parseInt(viewsParsed);
                String videoDate = scrapeVideoDate(videoID);
                String shortDate = videoDate.substring(0, 3);
                monthsAndVideoViewsMap.put(shortDate, monthsAndVideoViewsMap.get(shortDate) + viewsAsInt);
                String videoTitle = in.readLine();
                logger.info("video title: {}", videoTitle);
            }
            response.setLabels(labels);
            for (Map.Entry<String, Integer> entry : monthsAndVideoViewsMap.entrySet()) {
                datasets.add(entry.getValue().toString());
            }
            response.setDatasets(datasets);
            pr.waitFor();
            logger.info("ok!");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return response;
    }

    private String scrapeVideoDate(String videoID) {
        ProcessBuilder ps = new ProcessBuilder("python3", "get_video_by_id.py", videoID);
        String videoDate = "";
        try {
            logger.info("scraping video date for ID: {}", videoID);
            Process pr = ps.start();
            BufferedReader in = new BufferedReader(new InputStreamReader(pr.getInputStream()));
            videoDate = in.readLine();
            logger.info(videoDate);
            pr.waitFor();
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return videoDate;
    }
}
