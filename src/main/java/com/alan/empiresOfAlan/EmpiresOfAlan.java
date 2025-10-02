package com.alan.empiresOfAlan;

import com.alan.empiresOfAlan.api.EmpiresOfAlanAPI;
import com.alan.empiresOfAlan.commands.CommandManager;
import com.alan.empiresOfAlan.database.SQLiteManager;
import com.alan.empiresOfAlan.listeners.ChatListener;
import com.alan.empiresOfAlan.listeners.ClaimListener;
import com.alan.empiresOfAlan.listeners.PlayerListener;
import com.alan.empiresOfAlan.managers.*;
import com.alan.empiresOfAlan.util.AsyncExecutor;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public class EmpiresOfAlan extends JavaPlugin {
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;
    private AsyncExecutor asyncExecutor;
    private CommandManager commandManager;
    private BukkitTask taxTask;

    @Override
    public void onEnable() {
        // Create plugin data folder if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize utilities
        this.asyncExecutor = new AsyncExecutor(this);
        this.configManager = new ConfigManager(this);

        // Initialize database
        this.sqliteManager = new SQLiteManager(this);
        this.sqliteManager.initialize().thenRun(() -> {
            getLogger().info("Database initialized successfully!");

            // Load data from database
            // This will be implemented later
        });

        // Initialize managers
        initializeManagers();

        // Register commands
        this.commandManager = new CommandManager(this);

        // Register listeners
        registerListeners();

        // Start tax collection task (runs every 30 minutes)
        this.taxTask = getServer().getScheduler().runTaskTimer(this, () -> {
            TaxManager.getInstance().checkAndCollectTaxes();
        }, 20 * 60 * 30, 20 * 60 * 30); // 30 minutes in ticks

        getLogger().info("EmpiresOfAlan has been enabled!");
    }

    @Override
    public void onDisable() {
        // Cancel scheduled tasks
        if (taxTask != null) {
            taxTask.cancel();
        }

        // Save data to database
        // This will be implemented later

        // Close database connection
        if (sqliteManager != null) {
            sqliteManager.close();
        }

        // Shutdown async executor
        if (asyncExecutor != null) {
            asyncExecutor.shutdown();
        }

        getLogger().info("EmpiresOfAlan has been disabled!");
    }

    /**
     * Initialize all manager classes
     */
    private void initializeManagers() {
        try {
            // The order is important due to dependencies between managers
            ResidentManager.getInstance();
            TownManager.getInstance();
            NationManager.getInstance();
            ClaimManager.getInstance();
            TaxManager.getInstance().loadConfig(configManager);

            getLogger().info("All managers initialized successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize managers", e);
        }
    }

    /**
     * Register event listeners
     */
    private void registerListeners() {
        PluginManager pluginManager = getServer().getPluginManager();

        pluginManager.registerEvents(new ChatListener(this), this);
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new ClaimListener(this), this);

        getLogger().info("Event listeners registered successfully!");
    }

    /**
     * Get the config manager
     *
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the SQLite manager
     *
     * @return The SQLite manager
     */
    public SQLiteManager getSQLiteManager() {
        return sqliteManager;
    }

    /**
     * Get the async executor
     *
     * @return The async executor
     */
    public AsyncExecutor getAsyncExecutor() {
        return asyncExecutor;
    }

    /**
     * Get the plugin API
     *
     * @return The API instance
     */
    public EmpiresOfAlanAPI getAPI() {
        return EmpiresOfAlanAPI.getInstance(this);
    }
}