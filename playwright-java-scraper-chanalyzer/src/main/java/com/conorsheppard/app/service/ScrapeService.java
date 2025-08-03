package com.conorsheppard.app.service;

import com.conorsheppard.app.entity.YouTubeVideo;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.File;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.conorsheppard.app.utils.CookieUtils.*;

@Slf4j
@Service
public class ScrapeService {
    private final String channelURL = "https://youtube.com/${CHANNEL}/videos";
    private static final String COOKIES_FILE = "cookies.json";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/110.0.0.0 Safari/537.36";
    private static final String NUM_VIDS_LOCATOR = "#page-header span[class][dir=\"auto\"][style]:has-text('video')";
    private static final String MAX_VIDS_REGEX = "^[1-9] |^[1-9][0-9] |^[1-9][0-9]{2} ";
    private static final String HREF_LOCATOR = "a#thumbnail"; // <a> tag with id="thumbnail"
    private static final String VIDEOS_LOCATOR = "ytd-rich-item-renderer";
    private static final boolean IS_HEADLESS = true;
    private static final int VIDEOS_TIMEOUT = 10_000;
    private final HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, uuuu");

    @SneakyThrows
    public Flux<YouTubeVideo> scrapeChannel(String channelName, int maxVideos) {
        Set<String> videoLinks = new HashSet<>();
        return Flux.merge(
                httpClientScrape(channelName, videoLinks),
                scrapeWithPlaywright(channelName, maxVideos, videoLinks)
        ).distinct(YouTubeVideo::getVideoId);
    }

    @SneakyThrows
    private Flux<YouTubeVideo> httpClientScrape(String channelName, Set<String> videoLinks) {
        log.info("started initial scrape …");
        var channel = channelURL.replace("${CHANNEL}", channelName);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(channel))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/122.0.0.0 Safari/537.36")
                .header("Accept-Language", "en-US,en;q=0.9")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        Document doc = Jsoup.parse(response.body());
        String scriptContent = doc.select("script").stream()
                .map(Element::html)
                .filter(js -> js.contains("ytInitialData"))
                .findFirst()
                .orElseThrow();

        String jsonString = scriptContent.substring(scriptContent.indexOf('{'), scriptContent.lastIndexOf('}') + 1);

        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonString);

        return Flux.create((FluxSink<YouTubeVideo> sink) -> {
                    List<JsonNode> videoRenderers = new ArrayList<>();
                    rootNode.findParents("videoRenderer").forEach(node -> {
                        JsonNode renderer = node.get("videoRenderer");
                        if (renderer != null) {
                            videoRenderers.add(renderer);
                        }
                    });

                    for (JsonNode renderer : videoRenderers) {
                        String videoId = renderer.path("videoId").asText();
                        String title = renderer.path("title").path("runs").get(0).path("text").asText();
                        String views = renderer.path("viewCountText").path("simpleText").asText();
                        String published = parseRelativeDate(renderer.path("publishedTimeText").path("simpleText").asText());
                        String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

                        if (videoLinks.add(videoUrl)) {
                            sink.next(new YouTubeVideo()
                                    .setTitle(title)
                                    .setUrl(videoUrl)
                                    .setVideoId(videoId)
                                    .setViews(views)
                                    .setPublishedTime(published == null ? LocalDate.now().format(formatter) : published));
                        }
                    }

                    sink.complete();
                })
                .doOnSubscribe(sub -> log.info("Subscriber connected to initial scrape Flux"))
                .doOnNext(yt -> log.info("grabbing video: \"{}\" from initial scrape", yt.getTitle()))
                .share()
                .doAfterTerminate(() -> log.info("exiting httpClientScrape"));
    }

    private Flux<YouTubeVideo> scrapeWithPlaywright(String channelName, int maxVideos, Set<String> videoLinks) {
        return Flux.create((FluxSink<YouTubeVideo> sink) -> {
                    Playwright playwright = Playwright.create();
                    Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(IS_HEADLESS));
                    File cookiesFile = handleCookies();
                    try (BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                                    .setStorageStatePath(Path.of(COOKIES_FILE))
                                    .setExtraHTTPHeaders(Map.of("User-Agent", USER_AGENT, "Accept-Language", "en-US,en;q=0.9"))
//                    .setRecordVideoDir(Paths.get("videos/youtube/"))
//                    .setRecordVideoSize(1280, 720)
                    )) {

                        ensureCookiesFileExists();

                        if (cookiesFile.exists() && cookiesFile.length() > 0) {
                            context.addCookies(loadCookies());
                            log.info("Loaded cookies from file.");
                        }

                        try (Page page = context.newPage()) {
                            var channel = channelURL.replace("${CHANNEL}", channelName);
                            page.navigate(channel);
                            Locator acceptButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accept all"));
                            if (acceptButton.isVisible()) acceptButton.click();
                            getVideos(page, sink, maxVideos, videoLinks);
                            sink.complete();
                            log.info("Extracted {} video URLs", videoLinks.size());
                        }
                        saveCookies(context);
                    }
                    browser.close();
                })
                .doOnSubscribe(sub -> log.info("Subscriber connected"))
                .share() // replays results to new subscribers
                .doAfterTerminate(() -> log.info("exiting scrapeChannel"));
    }

    private static Set<String> getVideos(Page page, FluxSink<YouTubeVideo> sink, int maxVideos, Set<String> videoLinks) {
        var totalVideosText = page.locator(NUM_VIDS_LOCATOR).textContent();
        Pattern pattern = Pattern.compile(MAX_VIDS_REGEX);
        Matcher matcher = pattern.matcher(totalVideosText);
        Optional<Integer> totalVideosBelow1k = Optional.empty();
        if (matcher.find()) totalVideosBelow1k = Optional.of(Integer.parseInt(matcher.group(0).trim()));
        var videos = page.locator(VIDEOS_LOCATOR);
        videos.first().waitFor(new Locator.WaitForOptions().setTimeout(VIDEOS_TIMEOUT).setState(WaitForSelectorState.ATTACHED));

        int videosLoadedCheckpoint = 0; // Track the number of videos before scrolling
        var continueScraping = true;
        while (continueScraping) {
            List<Locator> videoElements = videos.all();
            int count = videoElements.size();

            for (int i = videosLoadedCheckpoint; i < count; i++) {
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
                    var videoId = href.substring(9, 20);
                    var videoUrl = "https://www.youtube.com/watch?=" + videoId;
                    if (videoLinks.add(videoUrl)) {
                        sink.next(new YouTubeVideo()
                                .setTitle(videoMetadata.title)
                                .setUrl(videoUrl)
                                .setVideoId(videoId)
                                .setViews(videoMetadata.views)
                                .setPublishedTime(videoMetadata.publishedDate));
                    } else {
                        log.info("skipping {}, already added", videoUrl);
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
            videos.nth(count + 1).waitFor(new Locator.WaitForOptions().setTimeout(VIDEOS_TIMEOUT).setState(WaitForSelectorState.ATTACHED));
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
        String publishedDate = parseRelativeDate(relativeDate);

        return new YouTubeVideoMetadata(title, views, publishedDate, relativeDate);
    }

    public static String parseRelativeDate(String text) {
        Pattern p = Pattern.compile("(\\d+)\\s+(second|minute|hour|day|week|month|year)s?\\s+ago", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            int amount = Integer.parseInt(m.group(1));
            String unit = m.group(2).toLowerCase();

            LocalDate now = LocalDate.now();
            LocalDate resultDate = switch (unit) {
                case "second", "minute", "hour" -> now; // approximate to today
                case "day" -> now.minusDays(amount);
                case "week" -> now.minusWeeks(amount);
                case "month" -> now.minusMonths(amount);
                case "year" -> now.minusYears(amount);
                default -> now;
            };

            return resultDate.format(formatter);
        }
        return null;
    }

    record YouTubeVideoMetadata(String title, String views, String publishedDate, String rawDateText) {
        public String toString() {
            return String.format("Title: %s%nViews: %s%nDate: %s (%s)", title, views,
                    publishedDate != null ? publishedDate : "Unknown", rawDateText);
        }
    }
}