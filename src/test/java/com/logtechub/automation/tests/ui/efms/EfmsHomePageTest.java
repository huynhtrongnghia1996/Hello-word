package com.logtechub.automation.tests.ui.efms;

import com.logtechub.automation.base.BaseUiTest;
import com.logtechub.automation.listeners.RetryAnalyzer;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Epic("eFMS")
@Feature("Home Page")
public class EfmsHomePageTest extends BaseUiTest {
    @Test(groups = {"ui", "smoke", "efms"}, retryAnalyzer = RetryAnalyzer.class)
    @Story("Open eFMS home page")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify eFMS home page can be opened on configured browser channels.")
    public void shouldOpenEfmsHomePage() {
        pages().efmsHomePage().open().waitUntilReady();

        assertThat(pages().efmsHomePage().currentUrl())
                .as("eFMS URL")
                .contains("uat-efms.logtechub.com");
    }
}
