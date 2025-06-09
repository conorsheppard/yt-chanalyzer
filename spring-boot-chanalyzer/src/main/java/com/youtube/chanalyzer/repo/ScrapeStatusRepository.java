package com.youtube.chanalyzer.repo;

import com.youtube.chanalyzer.entity.ScrapeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Optional;

public interface ScrapeStatusRepository extends JpaRepository<ScrapeStatus, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM ScrapeStatus s WHERE s.channelName = :channelName AND s.scrapeDate = :scrapeDate")
    Optional<ScrapeStatus> findWithLockByChannelNameAndScrapeDate(String channelName, LocalDate scrapeDate);

    Optional<ScrapeStatus> findByChannelNameAndScrapeDate(String channelName, LocalDate scrapeDate);
}
