package com.logtechub.automation.tests.ui.etms;

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

@Epic("eTMS")
@Feature("Home Page")
public class EtmsHomePageTest extends BaseUiTest {
    @Test(groups = {"ui", "smoke", "etms"}, retryAnalyzer = RetryAnalyzer.class)
    @Story("Open eTMS home page")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify eTMS staging home page can be opened on configured browser channels.")
    public void shouldOpenEtmsHomePage() {
        pages().etmsHomePage().open().waitUntilReady();

        assertThat(pages().etmsHomePage().currentUrl())
                .as("eTMS URL")
                .contains("staging-itllog-etms.logtechub.com");
    }
}
