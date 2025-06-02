import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import java.util.List;
import com.github.tomakehurst.wiremock.WireMockServer;


var urlList = List.of("1", "2", "3");

var publish = Flux.create(fluxSink -> {
            for (var url : urlList) {
                fluxSink.next(url);
            }
        });

var publisher = publish.publish();

publisher.subscribe(System.out::println);

var wireMockServer = new WireMockServer(8089);
wireMockServer.start();

StringBuilder sb = new StringBuilder();
for (int i = 0; i < 1000; i++) {
    sb.append(String.format("{\"id\":%d,\"data\":\"msg-%d\"}\n", i, i));
}

wireMockServer
        .stubFor(get(urlEqualTo("/api/v1/channels"))
        .willReturn(aResponse()
            .withHeader("Content-Type", "application/stream+json")
            .withBody(sb.toString())));
