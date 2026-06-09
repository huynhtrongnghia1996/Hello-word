package com.logtechub.automation.tests.ui;

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

@Epic("EFMS")
@Feature("UAT Home Page")
public class UatHomePageTest extends BaseUiTest {
    @Test(groups = {"ui", "smoke"}, retryAnalyzer = RetryAnalyzer.class)
    @Story("Open UAT home page")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify EFMS UAT home page can be opened on configured browser channels.")
    public void shouldOpenUatHomePage() {
        pages().homePage().open().waitUntilReady();

        assertThat(pages().homePage().currentUrl())
                .as("UAT home page URL")
                .contains("uat-efms.logtechub.com")
                .contains("/#/home");
    }
}
