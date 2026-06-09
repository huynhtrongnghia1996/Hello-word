package com.logtechub.automation.pages.common;

import com.logtechub.automation.pages.BasePage;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class LoginPage extends BasePage {
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

    public LoginPage(Page page) {
        super(page);
    }

    @Step("Login with configured account")
    public LoginPage login(String username, String password) {
        firstVisible(USERNAME_SELECTORS, "username input").fill(username);
        firstVisible(PASSWORD_SELECTORS, "password input").fill(password);
        firstVisible(SUBMIT_SELECTORS, "login submit button").click();
        waitForDomContentLoaded();
        return this;
    }

    public boolean isPasswordFieldVisible() {
        return findVisible(PASSWORD_SELECTORS) != null;
    }

    private Locator firstVisible(String[] selectors, String elementName) {
        Locator locator = findVisible(selectors);
        if (locator == null) {
            throw new IllegalStateException("Could not find visible " + elementName);
        }
        return locator;
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
