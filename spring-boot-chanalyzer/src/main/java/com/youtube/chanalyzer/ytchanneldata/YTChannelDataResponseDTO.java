package com.youtube.chanalyzer.ytchanneldata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class YTChannelDataResponseDTO {
    List<String> labels;
    List<String> datasets;
    Integer currentInterval;

    public YTChannelDataResponseDTO() {}

    public YTChannelDataResponseDTO(ArrayList<HashMap<String, String>> l) {
        var res = YTChannelDataController.sanitiseResponse(l);
        this.labels = res.labels;
        this.datasets = res.datasets;
    }

    public List<String> getLabels() {
        return this.labels;
    }

    public List<String> getDatasets() {
        return this.datasets;
    }

    public Integer getCurrentInterval() {
        return this.currentInterval;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }

    public YTChannelDataResponseDTO setCurrentInterval(Integer currentInterval) {
        this.currentInterval = currentInterval;
        return this;
    }
}
