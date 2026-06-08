package com.logtechub.automation.playwright;

import java.util.Arrays;
import java.util.Locale;

public enum SupportedBrowser {
    CHROME("chrome", "chrome"),
    EDGE("edge", "msedge");

    private final String value;
    private final String channel;

    SupportedBrowser(String value, String channel) {
        this.value = value;
        this.channel = channel;
    }

    public String value() {
        return value;
    }

    public String channel() {
        return channel;
    }

    public static SupportedBrowser from(String browserName) {
        if (browserName == null || browserName.trim().isEmpty()) {
            return CHROME;
        }

        String normalized = browserName.trim().toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(browser -> browser.value.equals(normalized) || browser.name().equalsIgnoreCase(normalized))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported browser: " + browserName + ". Use chrome or edge."));
    }
}
