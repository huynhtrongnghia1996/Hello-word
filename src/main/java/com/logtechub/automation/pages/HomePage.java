package com.logtechub.automation.pages;

import com.logtechub.automation.config.ConfigManager;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class HomePage extends BasePage {
    private final ConfigManager config = ConfigManager.getInstance();

    public HomePage(Page page) {
        super(page);
    }

    @Step("Open EFMS UAT home page")
    public HomePage open() {
        navigate(config.baseUrl());
        return this;
    }

    @Step("Wait for EFMS home page to be ready")
    public HomePage waitUntilReady() {
        waitForDomContentLoaded();
        return this;
    }
}
