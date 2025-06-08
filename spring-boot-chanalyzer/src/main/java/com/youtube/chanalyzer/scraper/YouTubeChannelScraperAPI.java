package com.youtube.chanalyzer.scraper;

import com.youtube.chanalyzer.dto.ChartJSDataResponseDTO;
import com.youtube.chanalyzer.dto.YouTubeVideoDTO;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
@Slf4j
@Component
public class YouTubeChannelScraperAPI implements ScraperAPI<ChartJSDataResponseDTO> {
    private final WebClient webClient;

    public Flux<ChartJSDataResponseDTO> getChannelVideoData(String channelName, int numVideos) {
        return getScrapeResponse(channelName, numVideos);
    }

    private Flux<ChartJSDataResponseDTO> getScrapeResponse(String channelName, int numVideos) {
        return webClient.get()
                .uri("?channel=" + channelName + "&numVideos=" + numVideos)
                .accept(MediaType.valueOf(MediaType.TEXT_EVENT_STREAM_VALUE))
                .retrieve()
                .bodyToFlux(YouTubeVideoDTO.class)
                .map(yt -> List.of(new HashMap<>(Map.of("uploadDate", yt.getPublishedTime(), "viewCount", yt.getViews()))))
                .map(ChartJSDataResponseDTO::new)
                .share();
    }

    public static ChartJSDataResponseDTO sortVideosIntoMonths(List<HashMap<String, String>> responseBody) {
        ChartJSDataResponseDTO response = new ChartJSDataResponseDTO();
        LinkedHashMap<String, Integer> monthsAndNumUploadsMap = new LinkedHashMap<>();
        LinkedHashMap<String, Double> monthsAndTotalViewsMap = new LinkedHashMap<>();
        List<String> videoDatesList = new ArrayList<>(), videoViewsList = new ArrayList<>(), avgVideoViewsList = new ArrayList<>();
        Map<String, List<String>> videoDatesMap = new LinkedHashMap<>(), videoViewsMap = new LinkedHashMap<>(), avgVideoViewsMap = new LinkedHashMap<>();

        for (HashMap<String, String> res : responseBody) {
            String videoDate = res.get("uploadDate");
            double viewCount = Double.parseDouble(res.get("viewCount").replaceAll(",", "").replace(" views", ""));
            Pattern pattern = Pattern.compile("( \\d{1,2},)");
            Matcher matcher = pattern.matcher(videoDate);
            var match = matcher.find();
            if (!match) log.error("Failed to parse date: {}", videoDate);
            String currentMonthAndYear = videoDate.replace(matcher.group(0), ",").replace("Premiered ", "");
            var currentMonthValue = monthsAndNumUploadsMap.get(currentMonthAndYear) == null ? 0 : monthsAndNumUploadsMap.get(currentMonthAndYear);
            var currentTotalViews = monthsAndTotalViewsMap.get(currentMonthAndYear) == null ? 0 : monthsAndTotalViewsMap.get(currentMonthAndYear);
            monthsAndTotalViewsMap.put(currentMonthAndYear, currentTotalViews + viewCount);
            monthsAndNumUploadsMap.put(currentMonthAndYear, currentMonthValue + 1);
        }
        List<String> labels = new ArrayList<>(monthsAndNumUploadsMap.keySet());
        response.setLabels(labels);

        for (Map.Entry<String, Integer> entry : monthsAndNumUploadsMap.entrySet()) {
            videoDatesList.add(entry.getValue().toString());
        }
        videoDatesMap.put("data", videoDatesList);

        for (Map.Entry<String, Double> entry : monthsAndTotalViewsMap.entrySet()) {
            double viewCountInMillions = entry.getValue() / 1_000_000.0;
            videoViewsList.add(Double.toString(viewCountInMillions));
        }
        videoViewsMap.put("data", videoViewsList);

        for (Map.Entry<String, Double> entry : monthsAndTotalViewsMap.entrySet()) {
            var month = entry.getKey();
            var views = entry.getValue();
            var avgViews = views / monthsAndNumUploadsMap.get(month);
            avgVideoViewsList.add(String.valueOf(avgViews));
        }
        avgVideoViewsMap.put("data", avgVideoViewsList);

        List<Map<String, List<String>>> datasets = Arrays.asList(videoDatesMap, videoViewsMap, avgVideoViewsMap);
        response.setDatasets(datasets);

        return response;
    }
}
