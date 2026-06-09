package com.logtechub.automation.pages;

import com.logtechub.automation.pages.common.LoginPage;
import com.logtechub.automation.pages.efms.EfmsHomePage;
import com.logtechub.automation.pages.etms.EtmsHomePage;
import com.microsoft.playwright.Page;

public class PageManager {
    private final Page page;
    private LoginPage loginPage;
    private EfmsHomePage efmsHomePage;
    private EtmsHomePage etmsHomePage;

    public PageManager(Page page) {
        this.page = page;
    }

    public LoginPage loginPage() {
        if (loginPage == null) {
            loginPage = new LoginPage(page);
        }
        return loginPage;
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
