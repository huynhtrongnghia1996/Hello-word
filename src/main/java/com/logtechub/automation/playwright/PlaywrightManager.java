package com.logtechub.automation.playwright;

import com.logtechub.automation.config.ConfigManager;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class PlaywrightManager {
    private static final Logger LOGGER = LogManager.getLogger(PlaywrightManager.class);
    private static final ThreadLocal<Playwright> PLAYWRIGHT = new ThreadLocal<>();
    private static final ThreadLocal<Browser> BROWSER = new ThreadLocal<>();
    private static final ThreadLocal<BrowserContext> CONTEXT = new ThreadLocal<>();
    private static final ThreadLocal<Page> PAGE = new ThreadLocal<>();
    private static final ThreadLocal<SupportedBrowser> BROWSER_TYPE = new ThreadLocal<>();

    private PlaywrightManager() {
    }

    public static Page start(SupportedBrowser supportedBrowser) {
        ConfigManager config = ConfigManager.getInstance();
        Playwright playwright = Playwright.create();

        Browser browser = playwright.chromium().launch(new com.microsoft.playwright.BrowserType.LaunchOptions()
                .setChannel(supportedBrowser.channel())
                .setHeadless(config.getBoolean("browser.headless", true))
                .setSlowMo(config.getLong("browser.slowMo", 0)));

        Browser.NewContextOptions contextOptions = new Browser.NewContextOptions()
                .setBaseURL(config.baseUrl())
                .setViewportSize(
                        config.getInt("browser.viewport.width", 1440),
                        config.getInt("browser.viewport.height", 900));

        if (config.getBoolean("browser.recordVideo", false)) {
            contextOptions.setRecordVideoDir(Paths.get(config.get("browser.video.dir", "target/videos")));
        }

        BrowserContext context = browser.newContext(contextOptions);
        context.setDefaultTimeout(config.getInt("browser.timeout", 30000));
        context.setDefaultNavigationTimeout(config.getInt("browser.navigationTimeout", 60000));

        if (config.getBoolean("browser.recordTrace", false)) {
            context.tracing().start(new Tracing.StartOptions()
                    .setScreenshots(true)
                    .setSnapshots(true)
                    .setSources(true));
        }

        Page page = context.newPage();

        PLAYWRIGHT.set(playwright);
        BROWSER.set(browser);
        CONTEXT.set(context);
        PAGE.set(page);
        BROWSER_TYPE.set(supportedBrowser);

        LOGGER.info("Started Playwright session. env={}, browser={}, url={}",
                config.environment(), supportedBrowser.value(), config.baseUrl());
        return page;
    }

    public static Page page() {
        Page page = PAGE.get();
        if (page == null) {
            throw new IllegalStateException("Playwright page has not been started for this thread.");
        }
        return page;
    }

    public static SupportedBrowser browserType() {
        return BROWSER_TYPE.get();
    }

    public static void stop(boolean testFailed) {
        ConfigManager config = ConfigManager.getInstance();
        BrowserContext context = CONTEXT.get();
        try {
            if (context != null && config.getBoolean("browser.recordTrace", false)) {
                Path tracePath = Paths.get(config.get("browser.trace.dir", "target/traces"), traceFileName(testFailed));
                Files.createDirectories(tracePath.getParent());
                context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                LOGGER.info("Saved Playwright trace: {}", tracePath);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not save Playwright trace", e);
        } finally {
            closeQuietly(CONTEXT.get());
            closeQuietly(BROWSER.get());
            closeQuietly(PLAYWRIGHT.get());
            CONTEXT.remove();
            BROWSER.remove();
            PLAYWRIGHT.remove();
            PAGE.remove();
            BROWSER_TYPE.remove();
        }
    }

    private static String traceFileName(boolean testFailed) {
        SupportedBrowser browser = browserType();
        String browserName = browser == null ? "unknown" : browser.value();
        String status = testFailed ? "failed" : "passed";
        return browserName + "-" + status + "-" + System.currentTimeMillis() + ".zip";
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (Exception e) {
            LOGGER.debug("Ignoring close failure", e);
        }
    }
}
