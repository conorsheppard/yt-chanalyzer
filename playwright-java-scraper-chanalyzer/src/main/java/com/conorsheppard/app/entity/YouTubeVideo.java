package com.conorsheppard.app.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class YouTubeVideo {
    private String title;
    private String url;
    private String videoId;
    private String views;
    private String publishedTime;
}
