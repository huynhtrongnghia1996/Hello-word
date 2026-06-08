package com.logtechub.automation.listeners;

import com.logtechub.automation.config.ConfigManager;
import com.logtechub.automation.playwright.PlaywrightManager;
import com.logtechub.automation.reporting.AllureAttachments;
import com.microsoft.playwright.Page;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestListener implements ITestListener {
    private static final Logger LOGGER = LogManager.getLogger(TestListener.class);

    @Override
    public void onTestFailure(ITestResult result) {
        LOGGER.error("Test failed: {}", result.getName(), result.getThrowable());
        attachFailureDetails(result);
    }

    private void attachFailureDetails(ITestResult result) {
        try {
            Page page = PlaywrightManager.page();
            AllureAttachments.attachText("Current URL", page.url());
            byte[] screenshot = page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setPath(screenshotPath(result)));
            AllureAttachments.attachPng("Failure screenshot", screenshot);
        } catch (Exception e) {
            LOGGER.warn("Could not capture failure artifacts", e);
        }

        if (result.getThrowable() != null) {
            AllureAttachments.attachText("Failure stacktrace", stackTrace(result.getThrowable()));
        }
    }

    private Path screenshotPath(ITestResult result) throws Exception {
        ConfigManager config = ConfigManager.getInstance();
        Path directory = Paths.get(config.get("test.screenshot.dir", "target/screenshots"));
        Files.createDirectories(directory);
        String fileName = result.getTestClass().getRealClass().getSimpleName()
                + "-" + result.getMethod().getMethodName()
                + "-" + System.currentTimeMillis() + ".png";
        return directory.resolve(fileName);
    }

    private String stackTrace(Throwable throwable) {
        StringBuilder builder = new StringBuilder();
        builder.append(throwable).append(System.lineSeparator());
        for (StackTraceElement element : throwable.getStackTrace()) {
            builder.append("    at ").append(element).append(System.lineSeparator());
        }
        return builder.toString();
    }
}
