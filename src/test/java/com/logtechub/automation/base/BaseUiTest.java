package com.logtechub.automation.base;

import com.logtechub.automation.config.ConfigManager;
import com.logtechub.automation.pages.PageManager;
import com.logtechub.automation.playwright.PlaywrightManager;
import com.logtechub.automation.playwright.SupportedBrowser;
import com.microsoft.playwright.Page;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

public abstract class BaseUiTest {
    private final ThreadLocal<PageManager> pageManager = new ThreadLocal<>();
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
        pageManager.set(new PageManager(page()));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        boolean failed = result.getStatus() == ITestResult.FAILURE;
        PlaywrightManager.stop(failed);
        pageManager.remove();
    }

    protected Page page() {
        return PlaywrightManager.page();
    }

    protected PageManager pages() {
        PageManager manager = pageManager.get();
        if (manager == null) {
            throw new IllegalStateException("PageManager has not been initialized for this test thread.");
        }
        return manager;
    }

    protected void loginWithConfiguredAccount() {
        pages().loginPage().login(config.accountUsername(), requiredAccountPassword());
    }

    private String requiredAccountPassword() {
        String password = config.accountPassword();
        if (password == null || password.trim().isEmpty()) {
            throw new SkipException("Set account.password or ACCOUNT_PASSWORD to run login tests.");
        }
        return password;
    }
}
