package com.youtube.chanalyzer.ytchanneldata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.youtube.chanalyzer.ytchanneldata.YTChannelResponseHandler.sortVideosIntoMonths;

public class YTChannelDataResponseDTO {
    List<String> labels;
    List<Map<String, List<String>>> datasets;
    Integer currentInterval;

    public YTChannelDataResponseDTO() {}

    public YTChannelDataResponseDTO(ArrayList<HashMap<String, String>> responseObject) {
        var res = sortVideosIntoMonths(responseObject);
        this.labels = res.labels;
        this.datasets = res.datasets;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public List<Map<String, List<String>>> getDatasets() {
        return this.datasets;
    }

    public Integer getCurrentInterval() {
        return this.currentInterval;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setDatasets(List<Map<String, List<String>>> datasets) {
        this.datasets = datasets;
    }

    public YTChannelDataResponseDTO setCurrentInterval(Integer currentInterval) {
        this.currentInterval = currentInterval;
        return this;
    }
}
