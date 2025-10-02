package com.alan.empiresOfAlan.util;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
    private File configFile;
    private final Map<String, FileConfiguration> languageFiles;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.languageFiles = new HashMap<>();
        this.loadConfig();
        this.loadLanguage("en_US");
    }

    /**
     * Load the main configuration file
     */
    public void loadConfig() {
        if (configFile == null) {
            configFile = new File(plugin.getDataFolder(), "config.yml");
        }

        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }

        config = YamlConfiguration.loadConfiguration(configFile);

        // Look for defaults in the jar
        InputStream defaultConfigStream = plugin.getResource("config.yml");
        if (defaultConfigStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultConfigStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
    }

    /**
     * Save the configuration to file
     */
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config to " + configFile);
            e.printStackTrace();
        }
    }

    /**
     * Load a language file
     *
     * @param lang Language code (e.g., "en_US")
     */
    public void loadLanguage(String lang) {
        File langFolder = new File(plugin.getDataFolder(), "lang");
        if (!langFolder.exists()) {
            langFolder.mkdirs();
        }

        File langFile = new File(langFolder, lang + ".yml");
        if (!langFile.exists()) {
            plugin.saveResource("lang/" + lang + ".yml", false);
        }

        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

        // Look for defaults in the jar
        InputStream defaultLangStream = plugin.getResource("lang/" + lang + ".yml");
        if (defaultLangStream != null) {
            YamlConfiguration defaultLang = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultLangStream, StandardCharsets.UTF_8));
            langConfig.setDefaults(defaultLang);
        }

        languageFiles.put(lang, langConfig);
    }

    /**
     * Get a message from the language file and process color codes
     *
     * @param lang Language code
     * @param path Message path
     * @param defaultMsg Default message if not found
     * @return The message with processed color codes
     */
    public String getMessage(String lang, String path, String defaultMsg) {
        FileConfiguration langConfig = languageFiles.get(lang);
        if (langConfig == null) {
            return ChatColor.translateAlternateColorCodes('&', defaultMsg);
        }
        String message = langConfig.getString(path, defaultMsg);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    /**
     * Get a message from the default language file (en_US) and process color codes
     *
     * @param path Message path
     * @param defaultMsg Default message if not found
     * @return The message with processed color codes
     */
    public String getMessage(String path, String defaultMsg) {
        return getMessage("en_US", path, defaultMsg);
    }

    /**
     * Get the main configuration
     *
     * @return FileConfiguration
     */
    public FileConfiguration getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    /**
     * Set a value in the configuration
     *
     * @param path Config path
     * @param value Value to set
     */
    public void setConfigValue(String path, Object value) {
        getConfig().set(path, value);
        saveConfig();
    }

    /**
     * Check if a language file is loaded
     *
     * @param lang Language code
     * @return true if loaded, false otherwise
     */
    public boolean isLanguageLoaded(String lang) {
        return languageFiles.containsKey(lang);
    }

    /**
     * Get all loaded language configurations
     *
     * @return Map of language code to configuration
     */
    public Map<String, FileConfiguration> getLanguageFiles() {
        return new HashMap<>(languageFiles);
    }

    /**
     * Reload all configurations
     */
    public void reload() {
        loadConfig();
        languageFiles.clear();
        loadLanguage("en_US");
    }
}