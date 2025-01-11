package com.youtube.chanalyzer.service;

import com.youtube.chanalyzer.dto.YTChannelDataResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.youtube.chanalyzer.ChanalyzerApplication.LOG;

@Service
public class YTChannelDataService {
    private final WebClient webClient;
    List<Integer> numVidsToScrapeList = Arrays.asList(1, 2, 4, 8, 16, 24, 32, 48, 64, 88);

    @Autowired
    public YTChannelDataService(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<YTChannelDataResponseDTO> initiateResponseStream(String channelUrl) {
        LOG.info("in initiateResponseStream: {}", channelUrl);
        var fluxFromIterable = Flux
                .fromIterable(numVidsToScrapeList)
                .log("after fromIterable")
                .flatMap(i -> getScrapeResponse(i, channelUrl))
                .log("after flatmap");

        fluxFromIterable.subscribe();

        return fluxFromIterable;
    }

    private Mono<YTChannelDataResponseDTO> getScrapeResponse(Integer numVideos, String channelUrl) {
        return webClient.get()
                .uri("?channelUrl=" + channelUrl + "&numVideos=" + numVideos)
                .retrieve()
                .bodyToMono(ArrayList.class)
                .log("after bodyToMono")
                .map(YTChannelDataResponseDTO::new)
                .log("after map to YTChannelDataResponseDTO::new")
                .map(yt -> yt.setCurrentInterval(numVideos));
    }

    public static YTChannelDataResponseDTO sortVideosIntoMonths(ArrayList<HashMap<String, String>> responseBody) {
        YTChannelDataResponseDTO response = new YTChannelDataResponseDTO();
        LinkedHashMap<String, Integer> monthsAndNumUploadsMap = new LinkedHashMap<>();
        LinkedHashMap<String, Double> monthsAndTotalViewsMap = new LinkedHashMap<>();
        List<String> videoDatesList = new ArrayList<>(), videoViewsList = new ArrayList<>(), avgVideoViewsList = new ArrayList<>();
        Map<String, List<String>> videoDatesMap = new LinkedHashMap<>(), videoViewsMap = new LinkedHashMap<>(), avgVideoViewsMap = new LinkedHashMap<>();

        for (HashMap<String, String> res : responseBody) {
            String videoDate = res.get("uploadDate");
            double viewCount = Double.parseDouble(res.get("viewCount").replaceAll(",", "").replace(" views", ""));
            Pattern pattern = Pattern.compile("( \\d{1,2},)");
            Matcher matcher = pattern.matcher(videoDate);
            matcher.find();
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
            var avgViews = views/monthsAndNumUploadsMap.get(month);
            avgVideoViewsList.add(String.valueOf(avgViews));
        }
        avgVideoViewsMap.put("data", avgVideoViewsList);

        List<Map<String, List<String>>> datasets = Arrays.asList(videoDatesMap, videoViewsMap, avgVideoViewsMap);
        response.setDatasets(datasets);

        return response;
    }
}
