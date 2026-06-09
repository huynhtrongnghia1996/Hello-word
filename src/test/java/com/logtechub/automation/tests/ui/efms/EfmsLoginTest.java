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
@Feature("Login")
public class EfmsLoginTest extends BaseUiTest {
    @Test(groups = {"ui", "login", "efms"}, retryAnalyzer = RetryAnalyzer.class)
    @Story("Login to eFMS")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Login to eFMS with the configured shared account.")
    public void shouldLoginToEfms() {
        pages().efmsHomePage().open().waitUntilReady();
        pages().efmsHomePage().login(accountUsername(), accountPassword());

        assertThat(pages().efmsHomePage().isPasswordFieldVisible())
                .as("Password field should not remain visible after successful eFMS login")
                .isFalse();
        assertThat(page().url()).contains("uat-efms.logtechub.com");
    }
}
