package com.youtube.chanalyzer.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "scrape_status", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"channelName", "scrapeDate"})
})
public class ScrapeStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String channelName;

    private LocalDate scrapeDate;

    private String status; // "in_progress", "completed", "failed"

    private LocalDateTime startedAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();
}