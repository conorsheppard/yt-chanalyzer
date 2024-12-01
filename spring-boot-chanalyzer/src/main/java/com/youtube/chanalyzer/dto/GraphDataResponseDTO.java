package com.youtube.chanalyzer.dto;

import java.util.List;

public class GraphDataResponseDTO {
    List<String> labels;
    List<String> datasets;

    public List<String> getLabels() {
        return this.labels;
    }

    public List<String> getDatasets() {
        return this.datasets;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setDatasets(List<String> datasets) {
        this.datasets = datasets;
    }
}
