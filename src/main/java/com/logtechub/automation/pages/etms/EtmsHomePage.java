package com.logtechub.automation.pages.etms;

import com.logtechub.automation.config.ConfigManager;
import com.logtechub.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class EtmsHomePage extends BasePage {
    private static final String[] USERNAME_SELECTORS = {
            "input[name='username']",
            "input[name='userName']",
            "input[id='username']",
            "input[id='userName']",
            "input[autocomplete='username']",
            "input[placeholder*='Username']",
            "input[placeholder*='User']",
            "input[type='email']",
            "input[type='text']"
    };

    private static final String[] PASSWORD_SELECTORS = {
            "input[name='password']",
            "input[id='password']",
            "input[autocomplete='current-password']",
            "input[placeholder*='Password']",
            "input[type='password']"
    };

    private static final String[] SUBMIT_SELECTORS = {
            "button[type='submit']",
            "input[type='submit']",
            "button:has-text('Login')",
            "button:has-text('Log in')",
            "button:has-text('Sign in')"
    };

    private final ConfigManager config = ConfigManager.getInstance();

    public EtmsHomePage(Page page) {
        super(page);
    }

    @Step("Open eTMS home page")
    public EtmsHomePage open() {
        navigate(config.etmsBaseUrl());
        return this;
    }

    @Step("Wait for eTMS home page to be ready")
    public EtmsHomePage waitUntilReady() {
        waitForDomContentLoaded();
        return this;
    }

    @Step("Login to eTMS")
    public EtmsHomePage login(String username, String password) {
        waitForVisible(USERNAME_SELECTORS, "eTMS username input").fill(username);
        waitForVisible(PASSWORD_SELECTORS, "eTMS password input").fill(password);
        waitForVisible(SUBMIT_SELECTORS, "eTMS login submit button").click();
        waitForDomContentLoaded();
        return this;
    }

    public boolean isPasswordFieldVisible() {
        return findVisible(PASSWORD_SELECTORS) != null;
    }

    private Locator waitForVisible(String[] selectors, String elementName) {
        long timeoutMillis = config.getLong("browser.timeout", 30000);
        long deadline = System.currentTimeMillis() + timeoutMillis;

        while (System.currentTimeMillis() < deadline) {
            Locator locator = findVisible(selectors);
            if (locator != null) {
                return locator;
            }
            page.waitForTimeout(250);
        }

        throw new IllegalStateException("Could not find visible " + elementName + " within " + timeoutMillis + " ms");
    }

    private Locator findVisible(String[] selectors) {
        for (String selector : selectors) {
            Locator locator = page.locator(selector).first();
            if (locator.count() > 0 && locator.isVisible()) {
                return locator;
            }
        }
        return null;
    }
}
