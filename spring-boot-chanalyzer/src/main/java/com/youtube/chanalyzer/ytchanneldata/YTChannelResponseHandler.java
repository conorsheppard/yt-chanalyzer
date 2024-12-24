package com.youtube.chanalyzer.ytchanneldata;

import java.sql.Array;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class YTChannelResponseHandler {
    public static YTChannelDataResponseDTO sortVideosIntoMonths(ArrayList<HashMap<String, String>> responseBody) {
        YTChannelDataResponseDTO response = new YTChannelDataResponseDTO();
        LinkedHashMap<String, Integer> monthsAndNumUploadsMap = new LinkedHashMap<>();
        LinkedHashMap<String, Double> monthsAndTotalViewsMap = new LinkedHashMap<>();
        List<String> videoDatesList = new ArrayList<>();
        Map<String, List<String>> videoDates = new LinkedHashMap<>();
        List<String> videoViewsList = new ArrayList<>();
        Map<String, List<String>> videoViews = new LinkedHashMap<>();

        for (HashMap<String, String> res : responseBody) {
            String videoDate = res.get("uploadDate");
            Double viewCount = Double.parseDouble(res.get("viewCount").replaceAll(",", "").replace(" views", ""));
            var viewCountInMillions = viewCount/Double.valueOf(1_000_000);
            Pattern pattern = Pattern.compile("( \\d{1,2},)");
            Matcher matcher = pattern.matcher(videoDate);
            matcher.find();
            String currentMonthAndYear = videoDate.replace(matcher.group(0), ",").replace("Premiered ", "");
            var currentMonthValue = monthsAndNumUploadsMap.get(currentMonthAndYear) == null ? 0 : monthsAndNumUploadsMap.get(currentMonthAndYear);
            var currentTotalViews = monthsAndTotalViewsMap.get(currentMonthAndYear) == null ? 0 : monthsAndTotalViewsMap.get(currentMonthAndYear);
            monthsAndTotalViewsMap.put(currentMonthAndYear, currentTotalViews + viewCountInMillions);
            monthsAndNumUploadsMap.put(currentMonthAndYear, currentMonthValue + 1);
        }
        List<String> labels = new ArrayList<>(monthsAndNumUploadsMap.keySet());
        response.setLabels(labels);

        for (Map.Entry<String, Integer> entry : monthsAndNumUploadsMap.entrySet()) {
            videoDatesList.add(entry.getValue().toString());
        }
        videoDates.put("data", videoDatesList);

        for (Map.Entry<String, Double> entry : monthsAndTotalViewsMap.entrySet()) {
            videoViewsList.add(entry.getValue().toString());
        }
        videoViews.put("data", videoViewsList);

        List<Map<String, List<String>>> datasets = Arrays.asList(videoDates, videoViews);

        response.setDatasets(datasets);

        return response;
    }
}
