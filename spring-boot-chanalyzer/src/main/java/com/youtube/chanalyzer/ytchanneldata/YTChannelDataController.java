package com.youtube.chanalyzer.ytchanneldata;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@CrossOrigin(origins = "*", allowedHeaders = "*")
@RestController
@RequestMapping("/api")
public class YTChannelDataController {
    @Autowired
    private Environment env;
    private final Logger logger = LoggerFactory.getLogger(YTChannelDataController.class);
    private final WebClient client = WebClient.create();
    private final int[] videoQuantityIntervals = new int[]{1, 2, 4, 8, 16, 32};

    @GetMapping("/health")
    public ResponseEntity getHealthCheck() {
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @GetMapping(path = "/channel", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<YTChannelDataResponseDTO> getChannelVideos(@RequestParam String channelUrl) {
        List<Flux> fluxList = getFluxes(channelUrl);
        fluxList.forEach(Flux::subscribe);
        Flux response = Flux.empty();
        for (var f : fluxList) response = Flux.concat(response, f);
        return response;
    }

    private List<Flux> getFluxes(String channelUrl) {
        List<Flux> fluxList = new ArrayList<>();
        for (int i : videoQuantityIntervals) fluxList.add(createWebClient(channelUrl, i));
        return fluxList;
    }

    Flux<YTChannelDataResponseDTO> createWebClient(String channelUrl, int numVideos) {
        return client.get()
                .uri(env.getProperty("scraper_api") + "/scrape?channelUrl=" + channelUrl + "&numVideos=" + numVideos)
                .retrieve()
                .bodyToFlux(ArrayList.class)
                .map(YTChannelDataResponseDTO::new)
                .map(yt -> yt.setCurrentInterval(numVideos));
    }

    public static YTChannelDataResponseDTO sanitiseResponse(ArrayList<HashMap<String, String>> responseBody) {
        YTChannelDataResponseDTO response = new YTChannelDataResponseDTO();
        Map<String, Integer> monthsAndNumUploadsMap = new HashMap<>();
        List<String> datasets = new ArrayList<>();

        for (HashMap<String, String> res : responseBody) {
            String videoDate = res.get("uploadDate");
            Pattern pattern = Pattern.compile("( \\d{1,2},)");
            Matcher matcher = pattern.matcher(videoDate);
            matcher.find();
            String currentMonthAndYear = videoDate.replace(matcher.group(0), ",");
            currentMonthAndYear = currentMonthAndYear.replace("Premiered ", "");
            var currentMonthValue = monthsAndNumUploadsMap.get(currentMonthAndYear) == null ? 0 : monthsAndNumUploadsMap.get(currentMonthAndYear);
            monthsAndNumUploadsMap.put(currentMonthAndYear, currentMonthValue + 1);
        }
        List<String> labels = new ArrayList<>(monthsAndNumUploadsMap.keySet());
        response.setLabels(labels);

        for (Map.Entry<String, Integer> entry : monthsAndNumUploadsMap.entrySet()) {
            datasets.add(entry.getValue().toString());
        }

        response.setDatasets(datasets);

        return response;
    }
}
