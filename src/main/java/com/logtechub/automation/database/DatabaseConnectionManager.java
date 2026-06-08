package com.logtechub.automation.database;

import com.logtechub.automation.config.ConfigManager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnectionManager {
    private DatabaseConnectionManager() {
    }

    public static Connection getConnection() throws SQLException {
        ConfigManager config = ConfigManager.getInstance();
        String url = config.get("db.url");
        String username = config.get("db.username");
        String password = config.get("db.password");

        if (isBlank(url)) {
            throw new IllegalStateException("Database URL is not configured. Set db.url or DB_URL.");
        }

        return DriverManager.getConnection(url, username, password);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
