package com.logtechub.automation.pages;

import com.logtechub.automation.pages.efms.EfmsHomePage;
import com.logtechub.automation.pages.etms.EtmsHomePage;
import com.microsoft.playwright.Page;

public class PageManager {
    private final Page page;
    private EfmsHomePage efmsHomePage;
    private EtmsHomePage etmsHomePage;

    public PageManager(Page page) {
        this.page = page;
    }

    public EfmsHomePage efmsHomePage() {
        if (efmsHomePage == null) {
            efmsHomePage = new EfmsHomePage(page);
        }
        return efmsHomePage;
    }

    public EtmsHomePage etmsHomePage() {
        if (etmsHomePage == null) {
            etmsHomePage = new EtmsHomePage(page);
        }
        return etmsHomePage;
    }
}
