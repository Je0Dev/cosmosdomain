package com.papel.imdb_clone.config;


import java.util.Properties;

/**
 * Application configuration management class.
 * It is a singleton class that manages the application configuration.
 */
public class ApplicationConfig {

    /*
    Volatile instance means that the instance variable is stored in the main memory and not in the cache of the CPU.
     */
    private static volatile ApplicationConfig instance;

    private final Properties properties;

    // Default configuration values
    private static final String DEFAULT_APP_TITLE = "IMDb Clone JavaFX";
    private static final String DEFAULT_APP_VERSION = "1.0";
    /**
     * Private constructor to prevent direct instantiation.
     * Loads the configuration during object creation
     */
    private ApplicationConfig() {
        this.properties = new Properties();
        this.properties.put("app.title", DEFAULT_APP_TITLE);
        this.properties.put("app.version", DEFAULT_APP_VERSION);
    }

    /**
     * Returns the singleton instance of ApplicationConfig.
     * Uses double-checked locking for thread safety.
     * @return The singleton instance of ApplicationConfig
     */
    public static ApplicationConfig getInstance() {
        ApplicationConfig result = instance;
        if (result == null) {
            // double-checked locking
            synchronized (ApplicationConfig.class) {
                result = instance;
                if (result == null) {
                    instance = result = new ApplicationConfig();

                }
            }
        }
        return result;
    }
    /**
     * Gets the application title from configuration.
     * @return The configured application title or the default title if not specified
     */
    public String getAppTitle() {
        return properties.getProperty("app.title", DEFAULT_APP_TITLE);
    }

}