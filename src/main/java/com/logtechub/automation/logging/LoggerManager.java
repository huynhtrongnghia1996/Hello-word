package com.logtechub.automation.logging;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class LoggerManager {
    private LoggerManager() {
    }

    public static Logger getLogger(Class<?> type) {
        return LogManager.getLogger(type);
    }
}
