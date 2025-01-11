package com.youtube.chanalyzer.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youtube.chanalyzer.service.YTChannelDataService.sortVideosIntoMonths;


@Data
@NoArgsConstructor
public class YTChannelDataResponseDTO {
    List<String> labels;
    List<Map<String, List<String>>> datasets;
    Integer currentInterval;

    public YTChannelDataResponseDTO(ArrayList<HashMap<String, String>> responseObject) {
        var res = sortVideosIntoMonths(responseObject);
        this.labels = res.labels;
        this.datasets = res.datasets;
    }

    public YTChannelDataResponseDTO setCurrentInterval(Integer currentInterval) {
        this.currentInterval = currentInterval;
        return this;
    }
}
