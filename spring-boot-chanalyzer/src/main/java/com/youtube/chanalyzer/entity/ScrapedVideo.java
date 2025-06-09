package com.youtube.chanalyzer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "scraped_videos", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"channel_name", "video_id", "scraped_at"})
})
public class ScrapedVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelName;
    private String videoTitle;
    private String videoId;
    private LocalDate publishedDate;
    private Integer views;
    private String monthLabel;
    private Instant scrapedAt = Instant.now(); // time the individual video was scraped at
    private LocalDate scrapedDate; // the day this scrape was done, helps when scrapes cross date boundaries
}
