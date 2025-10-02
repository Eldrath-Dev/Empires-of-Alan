package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.integrations.VaultIntegration;
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
    private EmpiresOfAlan plugin;

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
     * Set the plugin reference
     *
     * @param plugin The plugin instance
     */
    public void setPlugin(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    /**
     * Check and collect taxes for all towns and nations
     */
    public void checkAndCollectTaxes() {
        if (!taxesEnabled || plugin == null) {
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
        VaultIntegration vaultIntegration = plugin.getVaultIntegration();
        double totalCollected = 0;

        for (UUID residentId : town.getResidents()) {
            Resident resident = residentManager.getResident(residentId);

            if (resident == null) {
                continue;
            }

            // Skip the town owner
            if (residentId.equals(town.getOwnerId())) {
                continue;
            }

            // Calculate tax amount
            double taxAmount = town.getTaxRate();

            // Get the player if online
            Player player = Bukkit.getPlayer(residentId);

            // Check if resident can afford the tax (online or offline)
            if (vaultIntegration.has(residentId, taxAmount)) {
                // Withdraw tax from resident
                if (vaultIntegration.withdraw(residentId, taxAmount)) {
                    // Add to town bank
                    town.getBankAccount().deposit(taxAmount);
                    totalCollected += taxAmount;

                    // Notify the player if online
                    if (player != null && player.isOnline()) {
                        player.sendMessage(plugin.getConfigManager().getMessage("taxes.town-paid",
                                        "§6You paid §e{0} §6in town taxes to §e{1}")
                                .replace("{0}", vaultIntegration.format(taxAmount))
                                .replace("{1}", town.getName()));
                    }
                }
            } else {
                // Notify player they couldn't afford taxes
                if (player != null && player.isOnline()) {
                    player.sendMessage(plugin.getConfigManager().getMessage("taxes.cannot-afford",
                                    "§cYou could not afford to pay §e{0} §cin town taxes.")
                            .replace("{0}", vaultIntegration.format(taxAmount)));
                }
            }
        }

        // Notify town owner of total taxes collected
        if (totalCollected > 0) {
            Player owner = Bukkit.getPlayer(town.getOwnerId());
            if (owner != null && owner.isOnline()) {
                owner.sendMessage(plugin.getConfigManager().getMessage("taxes.town-collected",
                                "§6Your town collected §e{0} §6in taxes.")
                        .replace("{0}", vaultIntegration.format(totalCollected)));
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
        double totalCollected = 0;

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
                if (town.getBankAccount().withdraw(taxAmount)) {
                    // Add to nation bank
                    nation.getBankAccount().deposit(taxAmount);
                    totalCollected += taxAmount;

                    // Notify town members
                    notifyTownOfNationTax(town, nation, taxAmount);
                }
            } else {
                // Town cannot afford the tax - apply penalties or take other actions
                handleTownTaxDefault(town, nation, taxAmount);
            }
        }

        // Notify nation leader of total taxes collected
        if (totalCollected > 0) {
            Player leader = Bukkit.getPlayer(nation.getLeaderId());
            if (leader != null && leader.isOnline()) {
                leader.sendMessage(plugin.getConfigManager().getMessage("taxes.nation-collected",
                                "§6Your nation collected §e{0} §6in taxes.")
                        .replace("{0}", plugin.getVaultIntegration().format(totalCollected)));
            }
        }
    }

    /**
     * Notify town members of nation tax payment
     *
     * @param town The town
     * @param nation The nation
     * @param amount The tax amount
     */
    private void notifyTownOfNationTax(Town town, Nation nation, double amount) {
        Player owner = Bukkit.getPlayer(town.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(plugin.getConfigManager().getMessage("taxes.nation-paid",
                            "§6Your town paid §e{0} §6in nation taxes to §e{1}")
                    .replace("{0}", plugin.getVaultIntegration().format(amount))
                    .replace("{1}", nation.getName()));
        }
    }

    /**
     * Handle town defaulting on nation taxes
     *
     * @param town The town
     * @param nation The nation
     * @param amount The tax amount
     */
    private void handleTownTaxDefault(Town town, Nation nation, double amount) {
        // Notify town owner of tax default
        Player owner = Bukkit.getPlayer(town.getOwnerId());
        if (owner != null && owner.isOnline()) {
            owner.sendMessage(plugin.getConfigManager().getMessage("taxes.failed-to-pay",
                            "§cYour town failed to pay §e{0} §cin nation taxes to §e{1}")
                    .replace("{0}", plugin.getVaultIntegration().format(amount))
                    .replace("{1}", nation.getName()));
        }

        // Notify nation leader of tax default
        Player leader = Bukkit.getPlayer(nation.getLeaderId());
        if (leader != null && leader.isOnline()) {
            leader.sendMessage(plugin.getConfigManager().getMessage("taxes.town-defaulted",
                            "§cTown §e{0} §cfailed to pay §e{1} §cin nation taxes.")
                    .replace("{0}", town.getName())
                    .replace("{1}", plugin.getVaultIntegration().format(amount)));
        }

        // TODO: Implement additional penalties for tax defaulting
        // For example: loss of claim permissions, automatic removal from nation after multiple defaults, etc.
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