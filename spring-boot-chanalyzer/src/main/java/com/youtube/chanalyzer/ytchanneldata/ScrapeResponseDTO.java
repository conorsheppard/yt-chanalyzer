package com.youtube.chanalyzer.ytchanneldata;

import lombok.Data;

@Data
public class ScrapeResponseDTO {
    String videoId;
    String title;
    String uploadDate;
    String viewCount;
}
