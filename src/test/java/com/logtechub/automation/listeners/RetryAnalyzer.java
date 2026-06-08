package com.logtechub.automation.listeners;

import com.logtechub.automation.config.ConfigManager;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class RetryAnalyzer implements IRetryAnalyzer {
    private int retryCount;
    private final int maxRetries = ConfigManager.getInstance().getInt("test.retry.count", 0);

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetries) {
            retryCount++;
            return true;
        }
        return false;
    }
}
