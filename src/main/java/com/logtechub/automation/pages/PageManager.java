package com.logtechub.automation.pages;

import com.microsoft.playwright.Page;

public class PageManager {
    private final Page page;
    private HomePage homePage;

    public PageManager(Page page) {
        this.page = page;
    }

    public HomePage homePage() {
        if (homePage == null) {
            homePage = new HomePage(page);
        }
        return homePage;
    }
}
