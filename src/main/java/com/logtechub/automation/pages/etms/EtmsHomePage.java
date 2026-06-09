package com.logtechub.automation.pages.etms;

import com.logtechub.automation.config.ConfigManager;
import com.logtechub.automation.pages.BasePage;
import com.microsoft.playwright.Page;
import io.qameta.allure.Step;

public class EtmsHomePage extends BasePage {
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
}
