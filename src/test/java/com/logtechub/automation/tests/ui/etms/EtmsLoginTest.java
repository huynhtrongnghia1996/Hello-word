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
@Feature("Login")
public class EtmsLoginTest extends BaseUiTest {
    @Test(groups = {"ui", "login", "etms"}, retryAnalyzer = RetryAnalyzer.class)
    @Story("Login to eTMS")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Login to eTMS with the configured shared account.")
    public void shouldLoginToEtms() {
        pages().etmsHomePage().open().waitUntilReady();
        pages().etmsHomePage().login(accountUsername(), accountPassword());

        assertThat(pages().etmsHomePage().isPasswordFieldVisible())
                .as("Password field should not remain visible after successful eTMS login")
                .isFalse();
        assertThat(page().url()).contains("staging-itllog-etms.logtechub.com");
    }
}
