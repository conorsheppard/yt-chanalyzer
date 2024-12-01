package com.youtube.chanalyzer.model;

import lombok.Data;

@Data
public class YouTubeVideo {
    String name;
    Integer views;
    String url;
    String uploadDate;
}
