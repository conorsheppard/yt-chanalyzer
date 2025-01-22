package com.youtube.chanalyzer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youtube.chanalyzer.scraper.YouTubeChannelScraperAPI.sortVideosIntoMonths;


@Data
@NoArgsConstructor
public class ChartJSDataResponseDTO {
    List<String> labels;
    List<Map<String, List<String>>> datasets;
    Integer currentInterval;

    public ChartJSDataResponseDTO(List<HashMap<String, String>> responseObject) {
        var res = sortVideosIntoMonths(responseObject);
        this.labels = res.labels;
        this.datasets = res.datasets;
    }

    public ChartJSDataResponseDTO setCurrentInterval(Integer currentInterval) {
        this.currentInterval = currentInterval;
        return this;
    }
}
