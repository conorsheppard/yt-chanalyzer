package com.conorsheppard.app.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.AriaRole;
import com.microsoft.playwright.options.Cookie;
import com.microsoft.playwright.options.SameSiteAttribute;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

@Slf4j
public class CookieUtils {
    static final String COOKIES_FILE = "cookies.json";

    @SneakyThrows
    public static void ensureCookiesFileExists() {
        File file = new File(COOKIES_FILE);
        if (!file.exists() && !file.createNewFile()) log.error("error occurred creating new cookies file");
    }

    @SneakyThrows
    public static List<Cookie> loadCookies() {
        ObjectMapper mapper = new ObjectMapper();
        CookieWrapper cookieWrapper = mapper.readValue(new File(COOKIES_FILE), CookieWrapper.class);

        return cookieWrapper.cookies.stream()
                .map(cookieWithTitleCase -> new Cookie(cookieWithTitleCase.name, cookieWithTitleCase.value)
                        .setDomain(cookieWithTitleCase.domain)
                        .setPath(cookieWithTitleCase.path)
                        .setExpires(cookieWithTitleCase.expires)
                        .setHttpOnly(cookieWithTitleCase.httpOnly)
                        .setSecure(cookieWithTitleCase.secure)
                        .setSameSite(convertSameSite(cookieWithTitleCase.sameSite)))
                .toList();
    }

    public static SameSiteAttribute convertSameSite(String titleCaseSameSite) {
        if (titleCaseSameSite == null) {
            return null;
        }
        return SameSiteAttribute.valueOf(titleCaseSameSite.toUpperCase());
    }

    @SneakyThrows
    public static File handleCookies() {
        Path cookiesFilePath = Paths.get(COOKIES_FILE);
        if (!Files.exists(cookiesFilePath)) {
            Files.createFile(cookiesFilePath);
            log.info("Cookies file created: {}", cookiesFilePath);
        } else {
            log.info("Cookies file already exists.");
        }

        return new File(COOKIES_FILE);
    }

    @SneakyThrows
    public static void saveCookies(BrowserContext context) {
        ObjectMapper mapper = new ObjectMapper();
        List<CookieWithTitleCaseSameSite> formattedCookies = context.cookies().stream()
                .map(cookie -> new CookieWithTitleCaseSameSite(cookie, formatSameSite(cookie.sameSite)))
                .toList();

        Map<String, Object> cookiesData = Map.of("cookies", formattedCookies);
        mapper.writerWithDefaultPrettyPrinter()
                .writeValue(Paths.get(COOKIES_FILE).toFile(), cookiesData);
    }

    public static void acceptCookies(Page page) {
        Locator acceptButton = page.getByRole(AriaRole.BUTTON, new Page.GetByRoleOptions().setName("Accept all"));
        if (acceptButton.count() > 0) {
            acceptButton.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
            acceptButton.click();
        }
    }

    public static String formatSameSite(SameSiteAttribute sameSite) {
        if (sameSite == null) return "Lax"; // Default if missing
        // Convert enum value from UPPERCASE to Capitalized Case
        return sameSite.name().charAt(0) + sameSite.name().substring(1).toLowerCase();
    }

    public static class CookieWrapper {
        public List<CookieWithTitleCaseSameSite> cookies;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class CookieWithTitleCaseSameSite {
        public String name;
        public String value;
        public String domain;
        public String path;
        public double expires;
        public boolean httpOnly;
        public boolean secure;
        public String sameSite; // Correct format: "Strict", "Lax", or "None"

        public CookieWithTitleCaseSameSite(Cookie original, String titleCaseSameSite) {
            this.name = original.name;
            this.value = original.value;
            this.domain = original.domain;
            this.path = original.path;
            this.expires = original.expires;
            this.httpOnly = original.httpOnly;
            this.secure = original.secure;
            this.sameSite = titleCaseSameSite;
        }
    }
}
