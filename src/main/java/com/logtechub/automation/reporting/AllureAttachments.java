package com.logtechub.automation.reporting;

import io.qameta.allure.Attachment;

public final class AllureAttachments {
    private AllureAttachments() {
    }

    @Attachment(value = "{name}", type = "text/plain")
    public static String attachText(String name, String content) {
        return content;
    }

    @Attachment(value = "{name}", type = "application/json")
    public static String attachJson(String name, String content) {
        return content;
    }

    @Attachment(value = "{name}", type = "image/png")
    public static byte[] attachPng(String name, byte[] content) {
        return content;
    }
}
