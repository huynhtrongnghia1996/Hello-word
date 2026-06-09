package com.logtechub.automation.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Objects;
import java.util.Properties;

public final class ConfigManager {
    private static final String DEFAULT_ENV = "UAT";
    private static final ConfigManager INSTANCE = new ConfigManager();

    private final Properties properties = new Properties();
    private final String environment;

    private ConfigManager() {
        this.environment = normalizeEnvironment(System.getProperty("env", System.getenv().getOrDefault("ENV", DEFAULT_ENV)));
        loadProperties(environment);
    }

    public static ConfigManager getInstance() {
        return INSTANCE;
    }

    public String environment() {
        return environment;
    }

    public String get(String key) {
        return get(key, null);
    }

    public String get(String key, String defaultValue) {
        String systemValue = System.getProperty(key);
        if (isNotBlank(systemValue)) {
            return systemValue.trim();
        }

        String environmentValue = System.getenv(toEnvironmentKey(key));
        if (isNotBlank(environmentValue)) {
            return environmentValue.trim();
        }

        String propertyValue = properties.getProperty(key);
        if (isNotBlank(propertyValue)) {
            return propertyValue.trim();
        }

        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        String value = get(key);
        return isNotBlank(value) ? Integer.parseInt(value) : defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = get(key);
        return isNotBlank(value) ? Boolean.parseBoolean(value) : defaultValue;
    }

    public long getLong(String key, long defaultValue) {
        String value = get(key);
        return isNotBlank(value) ? Long.parseLong(value) : defaultValue;
    }

    public String baseUrl() {
        return Objects.requireNonNull(get("app.baseUrl", efmsBaseUrl()), "Missing required property: app.baseUrl");
    }

    public String efmsBaseUrl() {
        return Objects.requireNonNull(get("efms.baseUrl"), "Missing required property: efms.baseUrl");
    }

    public String etmsBaseUrl() {
        return Objects.requireNonNull(get("etms.baseUrl"), "Missing required property: etms.baseUrl");
    }

    public String apiBaseUri() {
        return get("api.baseUri", baseUrl());
    }

    public String accountUsername() {
        return Objects.requireNonNull(get("account.username"), "Missing required property: account.username");
    }

    public String accountPassword() {
        return get("account.password");
    }

    private void loadProperties(String env) {
        String resourcePath = "config/" + env.toLowerCase(Locale.ROOT) + ".properties";
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                throw new IllegalStateException("Configuration file not found: " + resourcePath);
            }
            properties.load(stream);
        } catch (IOException e) {
            throw new IllegalStateException("Could not load configuration file for environment: " + env, e);
        }
    }

    private String normalizeEnvironment(String env) {
        if (!isNotBlank(env)) {
            return DEFAULT_ENV;
        }
        return env.trim().toUpperCase(Locale.ROOT);
    }

    private String toEnvironmentKey(String key) {
        return key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
    }

    private boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
