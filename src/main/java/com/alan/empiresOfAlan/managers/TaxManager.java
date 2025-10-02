package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TaxManager {
    private static TaxManager instance;
    private long townTaxInterval; // in milliseconds
    private long nationTaxInterval; // in milliseconds
    private boolean taxesEnabled;

    private TaxManager() {
        this.townTaxInterval = TimeUnit.DAYS.toMillis(1); // Default: 1 day
        this.nationTaxInterval = TimeUnit.DAYS.toMillis(1); // Default: 1 day
        this.taxesEnabled = true;
    }

    public static TaxManager getInstance() {
        if (instance == null) {
            instance = new TaxManager();
        }
        return instance;
    }

    /**
     * Load tax settings from config
     *
     * @param configManager The config manager
     */
    public void loadConfig(ConfigManager configManager) {
        this.taxesEnabled = configManager.getConfig().getBoolean("taxes.enabled", true);
        this.townTaxInterval = configManager.getConfig().getLong("taxes.town-interval", 86400000); // Default: 1 day
        this.nationTaxInterval = configManager.getConfig().getLong("taxes.nation-interval", 86400000); // Default: 1 day
    }

    /**
     * Check and collect taxes for all towns and nations
     */
    public void checkAndCollectTaxes() {
        if (!taxesEnabled) {
            return;
        }

        long currentTime = System.currentTimeMillis();

        // Collect town taxes
        TownManager townManager = TownManager.getInstance();
        Map<UUID, Town> towns = townManager.getAllTowns();

        for (Town town : towns.values()) {
            if (currentTime - town.getLastTaxCollection() >= townTaxInterval) {
                collectTownTax(town);
                town.setLastTaxCollection(currentTime);
            }
        }

        // Collect nation taxes
        NationManager nationManager = NationManager.getInstance();
        Map<UUID, Nation> nations = nationManager.getAllNations();

        for (Nation nation : nations.values()) {
            if (currentTime - nation.getLastTaxCollection() >= nationTaxInterval) {
                collectNationTax(nation);
                nation.setLastTaxCollection(currentTime);
            }
        }
    }

    /**
     * Collect taxes for a town
     *
     * @param town The town
     */
    private void collectTownTax(Town town) {
        if (town.getTaxRate() <= 0) {
            return;
        }

        ResidentManager residentManager = ResidentManager.getInstance();

        for (UUID residentId : town.getResidents()) {
            Resident resident = residentManager.getResident(residentId);

            if (resident == null) {
                continue;
            }

            // Skip the town owner
            if (residentId.equals(town.getOwnerId())) {
                continue;
            }

            // Get the player if online
            Player player = Bukkit.getPlayer(residentId);

            if (player != null && player.isOnline()) {
                // TODO: Implement Vault integration to withdraw money
                // For now, we'll just assume taxes are collected
                double taxAmount = town.getTaxRate();

                // Add to town bank
                town.getBankAccount().deposit(taxAmount);

                // Notify the player
                player.sendMessage("§6You paid §e" + taxAmount + " §6in town taxes to §e" + town.getName());
            }
        }
    }

    /**
     * Collect taxes for a nation
     *
     * @param nation The nation
     */
    private void collectNationTax(Nation nation) {
        if (nation.getTaxRate() <= 0) {
            return;
        }

        TownManager townManager = TownManager.getInstance();

        for (UUID townId : nation.getTowns()) {
            Town town = townManager.getTown(townId);

            if (town == null) {
                continue;
            }

            // Skip the capital
            if (townId.equals(nation.getCapitalId())) {
                continue;
            }

            // Calculate tax amount (flat rate for now)
            double taxAmount = nation.getTaxRate();

            // Check if town can afford the tax
            if (town.getBankAccount().hasFunds(taxAmount)) {
                // Withdraw from town bank
                town.getBankAccount().withdraw(taxAmount);

                // Add to nation bank
                nation.getBankAccount().deposit(taxAmount);

                // TODO: Notify town members
            } else {
                // TODO: Handle town not being able to pay taxes
                // For now, we'll just skip taxing
            }
        }
    }

    /**
     * Get the town tax interval in milliseconds
     *
     * @return Town tax interval
     */
    public long getTownTaxInterval() {
        return townTaxInterval;
    }

    /**
     * Set the town tax interval in milliseconds
     *
     * @param interval The interval
     */
    public void setTownTaxInterval(long interval) {
        this.townTaxInterval = Math.max(TimeUnit.MINUTES.toMillis(5), interval); // Minimum 5 minutes
    }

    /**
     * Get the nation tax interval in milliseconds
     *
     * @return Nation tax interval
     */
    public long getNationTaxInterval() {
        return nationTaxInterval;
    }

    /**
     * Set the nation tax interval in milliseconds
     *
     * @param interval The interval
     */
    public void setNationTaxInterval(long interval) {
        this.nationTaxInterval = Math.max(TimeUnit.MINUTES.toMillis(5), interval); // Minimum 5 minutes
    }

    /**
     * Check if taxes are enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean areTaxesEnabled() {
        return taxesEnabled;
    }

    /**
     * Enable or disable taxes
     *
     * @param enabled Whether taxes should be enabled
     */
    public void setTaxesEnabled(boolean enabled) {
        this.taxesEnabled = enabled;
    }
}