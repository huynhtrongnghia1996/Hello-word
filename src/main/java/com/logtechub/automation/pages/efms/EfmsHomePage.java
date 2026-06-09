package com.logtechub.automation.pages.efms;

import com.logtechub.automation.config.ConfigManager;
import com.logtechub.automation.pages.BasePage;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class EfmsHomePage extends BasePage {
    private final ConfigManager config = ConfigManager.getInstance();

    public EfmsHomePage(Page page) {
        super(page);
    }

    @Step("Open eFMS home page")
    public EfmsHomePage open() {
        navigate(config.efmsBaseUrl());
        return this;
    }

    @Step("Wait for eFMS home page to be ready")
    public EfmsHomePage waitUntilReady() {
        waitForDomContentLoaded();
        return this;
    }
}
