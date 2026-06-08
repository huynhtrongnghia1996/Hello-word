package com.logtechub.automation.base;

import com.logtechub.automation.config.ConfigManager;
import com.logtechub.automation.playwright.PlaywrightManager;
import com.logtechub.automation.playwright.SupportedBrowser;
import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class BaseUiTest {
    protected final ConfigManager config = ConfigManager.getInstance();

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional("") String browserParameter) {
        String browser = System.getProperty("browser");
        if (browser == null || browser.trim().isEmpty()) {
            browser = browserParameter;
        }
        if (browser == null || browser.trim().isEmpty()) {
            browser = config.get("browser.default", "chrome");
        }

        PlaywrightManager.start(SupportedBrowser.from(browser));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        boolean failed = result.getStatus() == ITestResult.FAILURE;
        PlaywrightManager.stop(failed);
    }

    protected Page page() {
        return PlaywrightManager.page();
    }
}
