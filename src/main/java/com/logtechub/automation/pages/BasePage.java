package com.logtechub.automation.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.microsoft.playwright.options.LoadState;
import io.qameta.allure.Step;

public abstract class BasePage {
    protected final Page page;

    protected BasePage(Page page) {
        this.page = page;
    }

    @Step("Navigate to {url}")
    public void navigate(String url) {
        page.navigate(url, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
        waitForDomContentLoaded();
    }

    @Step("Wait for page DOM content loaded")
    public void waitForDomContentLoaded() {
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);
    }

    public String title() {
        return page.title();
    }

    public String currentUrl() {
        return page.url();
    }

    protected Locator locator(String selector) {
        return page.locator(selector);
    }

    protected void click(String selector) {
        locator(selector).click();
    }

    protected void fill(String selector, String value) {
        locator(selector).fill(value);
    }

    protected String text(String selector) {
        return locator(selector).textContent();
    }
}
