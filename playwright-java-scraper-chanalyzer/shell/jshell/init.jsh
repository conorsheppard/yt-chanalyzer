import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.nio.file.*;

String channel = "https://youtube.com/@NASA/videos";
String jsonString = Files.readString(Path.of("src/test/resources/json/test-ytInitialData-json-string.json"));

ObjectMapper mapper = new ObjectMapper();
JsonNode rootNode = mapper.readTree(jsonString);

List<JsonNode> videoRenderers = new ArrayList<>();
rootNode.findParents("videoRenderer").forEach(node -> {
    JsonNode renderer = node.get("videoRenderer");
    if (renderer != null) {
        videoRenderers.add(renderer);
    }
});
Set<String> videoLinks = new HashSet<String>();
for(JsonNode renderer : videoRenderers) {
    String videoId = renderer.path("videoId").asText();
    String title = renderer.path("title").path("runs").get(0).path("text").asText();
    String views = renderer.path("viewCountText").path("simpleText").asText();
    String published = renderer.path("publishedTimeText").path("simpleText").asText();
    String videoUrl = "https://www.youtube.com/watch?v=" + videoId;

    if (videoLinks.add(videoUrl)) {
        System.out.println((new YouTubeVideo()
        .setTitle(title)
        .setUrl(videoUrl)
        .setViews(views)
        .setPublishedTime(published)));
    }
}