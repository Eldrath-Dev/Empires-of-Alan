package com.alan.empiresOfAlan;

import com.alan.empiresOfAlan.database.SQLiteManager;
import com.alan.empiresOfAlan.managers.*;
import com.alan.empiresOfAlan.util.AsyncExecutor;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.logging.Level;

public class EmpiresOfAlan extends JavaPlugin {
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;
    private AsyncExecutor asyncExecutor;
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
            // This will be implemented in Phase 2
        });

        // Initialize managers
        initializeManagers();

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
        // This will be implemented in Phase 2

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
}