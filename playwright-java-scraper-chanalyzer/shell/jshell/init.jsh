import com.conorsheppard.app.web.WebClient;
import com.conorsheppard.app.web.JSoupWebClient;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import java.nio.file.*;

WebClient webClient = new JSoupWebClient();
String channel = "https://youtube.com/@NASA/videos";

