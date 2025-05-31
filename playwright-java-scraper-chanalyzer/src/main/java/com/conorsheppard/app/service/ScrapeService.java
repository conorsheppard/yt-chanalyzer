package com.conorsheppard.app.service;

import com.conorsheppard.app.entity.YouTubeVideo;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.conorsheppard.app.utils.CookieUtils.*;

@Slf4j
@Service
public class ScrapeService {
    private static final String COOKIES_FILE = "cookies.json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";
    private static final String NUM_VIDS_LOCATOR = "#page-header span[class][dir=\"auto\"][style]:has-text('video')";
    private static final String MAX_VIDS_REGEX = "^[1-9] |^[1-9][0-9] |^[1-9][0-9]{2} ";
    private static final String HREF_LOCATOR = "a#thumbnail"; // <a> tag with id="thumbnail"
    private static final String VIDEOS_LOCATOR = "ytd-rich-item-renderer";
    private static final boolean IS_HEADLESS = true;

    @SneakyThrows
    public Flux<YouTubeVideo> scrapeChannel(String channelName, int maxVideos) {
        return Flux.create((FluxSink<YouTubeVideo> sink) -> {

            Playwright playwright = Playwright.create();
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(IS_HEADLESS));
            File cookiesFile = handleCookies();
            try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                    .setStorageStatePath(Path.of(COOKIES_FILE))
                    .setExtraHTTPHeaders(Map.of("User-Agent", USER_AGENT, "Accept-Language", "en-US,en;q=0.9"))
                    .setRecordVideoDir(Paths.get("videos/youtube/"))
                    .setRecordVideoSize(1280, 720))) {

                ensureCookiesFileExists();

                if (cookiesFile.exists() && cookiesFile.length() > 0) {
                    context.addCookies(loadCookies());
                    log.info("Loaded cookies from file.");
                }

                try (Page page = context.newPage()) {
                    var channelURL = "https://youtube.com/${CHANNEL}/videos";
                    var channel = channelURL.replace("${CHANNEL}", channelName);
                    page.navigate(channel);
                    Locator acceptButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accept all"));
                    if (acceptButton.isVisible()) acceptButton.click();
                    var videoLinks = getVideos(page, sink, maxVideos);
                    sink.complete();
                    log.info("Extracted {} video URLs", videoLinks.size());
                }
                log.info("saving cookies …");
                saveCookies(context);
                log.info("cookies saved …");
            }
            log.info("closing browser …");
            browser.close();
            log.info("browser closed …");
        })
        .doOnSubscribe(_ -> log.info("Subscriber connected"))
        .share()
        .doAfterTerminate(() -> log.info("exiting scrapeChannel")); // replays results to new subscribers
    }

    private static Set<String> getVideos(Page page, FluxSink<YouTubeVideo> sink, int maxVideos) {
        var totalVideosText = page.locator(NUM_VIDS_LOCATOR).textContent();
        Pattern pattern = Pattern.compile(MAX_VIDS_REGEX);
        Matcher matcher = pattern.matcher(totalVideosText);
        Optional<Integer> totalVideosBelow1k = Optional.empty();
        if (matcher.find()) totalVideosBelow1k = Optional.of(Integer.parseInt(matcher.group(0).trim()));
        var videos = page.locator(VIDEOS_LOCATOR);
        videos.first().waitFor(new Locator.WaitForOptions().setTimeout(5000).setState(WaitForSelectorState.ATTACHED));
        Set<String> videoLinks = new HashSet<>();

        int videosLoadedCheckpoint = 0; // Track the number of videos before scrolling
        var continueScraping = true;
        while (continueScraping) {
            List<Locator> videoElements = videos.all();
            int count = videoElements.size();

            for (int i = videosLoadedCheckpoint; i < count; i++) {
                log.info("i: {}", i);
                var video = videoElements.get(i);
                var vidInfoInnerText = "";
                try {
                    video.waitFor(new Locator.WaitForOptions().setTimeout(500).setState(WaitForSelectorState.VISIBLE));
                    vidInfoInnerText = video.innerText();
                } catch (PlaywrightException e) {
                    log.warn("Skipping video at index {} due to error: {}", i, e.getMessage());
                    continue;
                }
                var videoMetadata = extractMetadata(vidInfoInnerText);
                var href = video.locator(HREF_LOCATOR).first().getAttribute("href");
                if (href != null && href.contains("watch")) {
                    var videoUrl = "https://www.youtube.com" + href.substring(0, 20);
                    if (videoLinks.add(videoUrl)) {
                        log.info("added video: {}", videoUrl);
                        sink.next(new YouTubeVideo()
                                .setTitle(videoMetadata.title)
                                .setUrl(videoUrl)
                                .setViews(videoMetadata.views)
                                .setPublishedTime(videoMetadata.rawDateText));
                    }
                    if (videoLinks.size() == maxVideos) {
                        continueScraping = false;
                        break;
                    }
                }
            }

            log.info("Extracted {} unique videos so far...", videoLinks.size());

            // Stop scrolling if no new videos are loaded
            if (count == videosLoadedCheckpoint || totalVideosBelow1k.isPresent() && videoLinks.size() == totalVideosBelow1k.get()) {
                log.info("No more videos to load. Stopping scroll.");
                break;
            }

            videosLoadedCheckpoint = count;

            log.info("scrolling …");
            page.evaluate("window.scrollTo(0, document.documentElement.scrollHeight)");
            log.info("waiting for videos to load …");
            videos.nth(count + 1).waitFor(new Locator.WaitForOptions().setTimeout(5000).setState(WaitForSelectorState.ATTACHED));
            log.info("videos loaded");
        }
        return videoLinks;
    }

    public static YouTubeVideoMetadata extractMetadata(String input) {
        String[] lines = input.split("\\R"); // Split by newlines

        // 1. Extract title: take the last line before the one containing "views"
        String title = "";
        int viewsLineIndex = -1;
        Pattern viewsPattern = Pattern.compile("\\b\\d+(?:[.,]\\d+)?[KM]? views?\\b", Pattern.CASE_INSENSITIVE);
        for (int i = 0; i < lines.length; i++) {
            if (viewsPattern.matcher(lines[i]).find()) { // checking if lines at i is the views count string
                viewsLineIndex = i;
                break;
            }
        }
        if (viewsLineIndex > 0) {
            title = lines[viewsLineIndex - 1].trim();
        }

        // 2. Extract views
        String views = viewsLineIndex != -1 ? lines[viewsLineIndex].trim() : "";

        // 3. Extract relative date (line after views)
        String relativeDate = (viewsLineIndex + 1 < lines.length) ? lines[viewsLineIndex + 1].trim() : "";

        // 4. Convert relative date to LocalDate (approximate)
        LocalDate publishedDate = parseRelativeDate(relativeDate);

        return new YouTubeVideoMetadata(title, views, publishedDate, relativeDate);
    }

    public static LocalDate parseRelativeDate(String text) {
        Pattern p = Pattern.compile("(\\d+)\\s+(second|minute|hour|day|week|month|year)s?\\s+ago", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            int amount = Integer.parseInt(m.group(1));
            String unit = m.group(2).toLowerCase();

            LocalDate now = LocalDate.now();
            switch (unit) {
                case "second", "minute", "hour":
                    return now; // approximate to today
                case "day":
                    return now.minusDays(amount);
                case "week":
                    return now.minusWeeks(amount);
                case "month":
                    return now.minusMonths(amount);
                case "year":
                    return now.minusYears(amount);
            }
        }
        return null;
    }

    record YouTubeVideoMetadata(String title, String views, LocalDate publishedDate, String rawDateText) {
        public String toString() {
            return String.format("Title: %s%nViews: %s%nDate: %s (%s)", title, views,
                    publishedDate != null ? publishedDate : "Unknown", rawDateText);
        }
    }
}