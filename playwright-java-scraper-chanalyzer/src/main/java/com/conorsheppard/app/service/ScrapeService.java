package com.conorsheppard.app.service;

import com.conorsheppard.app.entity.YouTubeVideo;
import com.microsoft.playwright.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.List;

@Service
public class ScrapeService {

    public Flux<YouTubeVideo> scrapeChannel(String channelName, int maxVideos) {
        return Flux.create(sink -> {
            try (Playwright playwright = Playwright.create()) {
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                Page page = browser.newPage();

                String url = "https://www.youtube.com/@" + channelName + "/videos";
                page.navigate(url);
                page.waitForSelector("ytd-grid-video-renderer", new Page.WaitForSelectorOptions().setTimeout(10000));

                List<ElementHandle> videos = page.querySelectorAll("ytd-grid-video-renderer");
                int count = Math.min(videos.size(), maxVideos);

                for (int i = 0; i < count; i++) {
                    ElementHandle video = videos.get(i);
                    String title = video.querySelector("#video-title").innerText();
                    String href = video.querySelector("#video-title").getAttribute("href");
                    String publishedTime = video.querySelector("#metadata-line span").innerText();

                    sink.next(new YouTubeVideo(title, "https://www.youtube.com" + href, publishedTime));
                }

                sink.complete();
                browser.close();
            } catch (Exception e) {
                sink.error(new RuntimeException("Failed to scrape channel: " + e.getMessage(), e));
            }
        });
    }
}