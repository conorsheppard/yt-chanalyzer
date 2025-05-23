package com.conorsheppard.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class YouTubeVideo {
    private String title;
    private String url;
    private String views;
    private String publishedTime;
}
