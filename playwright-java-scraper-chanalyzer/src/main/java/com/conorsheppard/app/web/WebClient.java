package com.conorsheppard.app.web;

import org.jsoup.nodes.Document;
import java.io.IOException;

public interface WebClient {
    Document fetch(String url) throws IOException;
}
