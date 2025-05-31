package com.youtube.chanalyzer.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class YouTubeVideoDTO {
    private String title;
    private String url;
    private String views;
    private String publishedTime;
}
