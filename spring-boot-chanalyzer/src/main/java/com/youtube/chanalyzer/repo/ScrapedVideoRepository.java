package com.youtube.chanalyzer.repo;

import com.youtube.chanalyzer.entity.ScrapedVideo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ScrapedVideoRepository extends JpaRepository<ScrapedVideo, Long> {
    List<ScrapedVideo> findByChannelNameAndScrapedAt(String channelName, LocalDate scrapedAt);
}

