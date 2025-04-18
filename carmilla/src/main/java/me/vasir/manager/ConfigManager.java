package me.vasir.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class ConfigManager {

    private static final String CONFIG_FILE = "config.properties";
    private final Properties properties = new Properties();

    public ConfigManager() {
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException ex) {
            throw new RuntimeException("Config file could not be loaded: " + ex);
        }
    }

    // Tek bir ayarı almak için metod
    public String getConfig(String key) {return properties.getProperty(key);}
    public void setConfig(String key, String value) {
        properties.setProperty(key, value);
        saveConfig();
    }

    public void saveConfig() {
        try (FileOutputStream output = new FileOutputStream(CONFIG_FILE)) {
            properties.store(output, null);
        } catch (IOException ex) {
            throw new RuntimeException("Config file could not be saved: " + ex);
        }
    }
}
