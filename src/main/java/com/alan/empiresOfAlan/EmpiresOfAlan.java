package com.alan.empiresOfAlan;

import com.alan.empiresOfAlan.api.EmpiresOfAlanAPI;
import com.alan.empiresOfAlan.commands.CommandManager;
import com.alan.empiresOfAlan.database.SQLiteManager;
import com.alan.empiresOfAlan.database.dao.ClaimDAO;
import com.alan.empiresOfAlan.database.dao.NationDAO;
import com.alan.empiresOfAlan.database.dao.ResidentDAO;
import com.alan.empiresOfAlan.database.dao.TownDAO;
import com.alan.empiresOfAlan.integrations.VaultIntegration;
import com.alan.empiresOfAlan.listeners.ChatListener;
import com.alan.empiresOfAlan.listeners.ClaimListener;
import com.alan.empiresOfAlan.listeners.PlayerListener;
import com.alan.empiresOfAlan.managers.*;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.util.AsyncExecutor;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class EmpiresOfAlan extends JavaPlugin {
    private ConfigManager configManager;
    private SQLiteManager sqliteManager;
    private AsyncExecutor asyncExecutor;
    private CommandManager commandManager;
    private VaultIntegration vaultIntegration;
    private BukkitTask taxTask;
    private EmpiresOfAlanAPI api;

    // Database DAOs
    private ResidentDAO residentDAO;
    private TownDAO townDAO;
    private NationDAO nationDAO;
    private ClaimDAO claimDAO;

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
        this.residentDAO = new ResidentDAO(this);
        this.townDAO = new TownDAO(this);
        this.nationDAO = new NationDAO(this);
        this.claimDAO = new ClaimDAO(this);

        this.sqliteManager.initialize().thenRun(() -> {
            getLogger().info("Database initialized successfully!");
            // Load data from database
            loadData();
        });

        // Initialize managers
        initializeManagers();

        // Initialize Vault integration
        this.vaultIntegration = VaultIntegration.getInstance(this);

        // Initialize API
        this.api = EmpiresOfAlanAPI.getInstance(this);

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
        // Save data to database
        saveData();

        // Cancel scheduled tasks
        if (taxTask != null) {
            taxTask.cancel();
        }

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

            TaxManager taxManager = TaxManager.getInstance();
            taxManager.loadConfig(configManager);
            taxManager.setPlugin(this);

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
     * Load data from database
     */
    private void loadData() {
        try {
            // Load residents first
            Map<UUID, Resident> residents = residentDAO.loadAllResidents();
            for (Resident resident : residents.values()) {
                ResidentManager.getInstance().getResidents().put(resident.getUuid(), resident);
            }

            // Load towns
            Map<UUID, Town> towns = townDAO.loadAllTowns();
            for (Town town : towns.values()) {
                TownManager.getInstance().getTowns().put(town.getId(), town);
                TownManager.getInstance().getTownNameToId().put(town.getName().toLowerCase(), town.getId());
            }

            // Load nations
            Map<UUID, Nation> nations = nationDAO.loadAllNations();
            for (Nation nation : nations.values()) {
                NationManager.getInstance().getNations().put(nation.getId(), nation);
                NationManager.getInstance().getNationNameToId().put(nation.getName().toLowerCase(), nation.getId());
            }

            // Load claims
            Map<UUID, Claim> claims = claimDAO.loadAllClaims();
            for (Claim claim : claims.values()) {
                ClaimManager.getInstance().getClaims().put(claim.getId(), claim);
                ClaimManager.getInstance().getLocationToClaimId().put(claim.getLocationKey(), claim.getId());
            }

            getLogger().info("Loaded " + residents.size() + " residents, " + towns.size() + " towns, " + nations.size() + " nations, and " + claims.size() + " claims.");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to load data from database", e);
        }
    }

    /**
     * Save data to database
     */
    private void saveData() {
        try {
            // Save residents
            ResidentManager residentManager = ResidentManager.getInstance();
            for (Resident resident : residentManager.getAllResidents().values()) {
                residentDAO.saveResident(resident);
            }

            // Save towns
            TownManager townManager = TownManager.getInstance();
            for (Town town : townManager.getAllTowns().values()) {
                townDAO.saveTown(town);
            }

            // Save nations
            NationManager nationManager = NationManager.getInstance();
            for (Nation nation : nationManager.getAllNations().values()) {
                nationDAO.saveNation(nation);
            }

            // Save claims
            ClaimManager claimManager = ClaimManager.getInstance();
            for (Claim claim : claimManager.getAllClaims().values()) {
                claimDAO.saveClaim(claim);
            }

            getLogger().info("Data saved successfully!");
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to save data to database", e);
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

    /**
     * Get the Vault integration
     *
     * @return The VaultIntegration instance
     */
    public VaultIntegration getVaultIntegration() {
        return vaultIntegration;
    }

    /**
     * Get the plugin API
     *
     * @return The API instance
     */
    public EmpiresOfAlanAPI getAPI() {
        return api;
    }

    /**
     * Get the Resident DAO
     *
     * @return The ResidentDAO instance
     */
    public ResidentDAO getResidentDAO() {
        return residentDAO;
    }

    /**
     * Get the Town DAO
     *
     * @return The TownDAO instance
     */
    public TownDAO getTownDAO() {
        return townDAO;
    }

    /**
     * Get the Nation DAO
     *
     * @return The NationDAO instance
     */
    public NationDAO getNationDAO() {
        return nationDAO;
    }

    /**
     * Get the Claim DAO
     *
     * @return The ClaimDAO instance
     */
    public ClaimDAO getClaimDAO() {
        return claimDAO;
    }
}