package com.conorsheppard.app.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import java.io.IOException;

public class JSoupWebClient implements WebClient {
    @Override
    public Document fetch(String url) throws IOException {
        return Jsoup.connect(url).timeout(5000).get();
    }

}
