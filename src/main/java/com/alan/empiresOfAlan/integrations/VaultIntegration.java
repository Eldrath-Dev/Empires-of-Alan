package com.alan.empiresOfAlan.integrations;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.UUID;
import java.util.logging.Level;

/**
 * Handles integration with the Vault economy API
 */
public class VaultIntegration {
    private static VaultIntegration instance;
    private final EmpiresOfAlan plugin;
    private Economy economy;
    private boolean enabled;

    private VaultIntegration(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        this.enabled = setupEconomy();

        if (enabled) {
            plugin.getLogger().info("Vault economy integration enabled successfully!");
        } else {
            plugin.getLogger().warning("Vault economy integration could not be enabled. Economy features will be disabled.");
        }
    }

    /**
     * Get the singleton instance
     *
     * @param plugin The plugin instance
     * @return The VaultIntegration instance
     */
    public static VaultIntegration getInstance(EmpiresOfAlan plugin) {
        if (instance == null) {
            instance = new VaultIntegration(plugin);
        }
        return instance;
    }

    /**
     * Setup the economy integration
     *
     * @return true if successful, false otherwise
     */
    private boolean setupEconomy() {
        if (Bukkit.getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Check if the economy integration is enabled
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Get the economy instance
     *
     * @return The economy instance
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Format a currency amount
     *
     * @param amount The amount to format
     * @return Formatted amount string
     */
    public String format(double amount) {
        if (!enabled) {
            return String.format("%.2f", amount);
        }
        return economy.format(amount);
    }

    /**
     * Check if a player has enough money
     *
     * @param player The player
     * @param amount The amount to check
     * @return true if player has enough, false otherwise
     */
    public boolean has(Player player, double amount) {
        if (!enabled) {
            return true; // If economy is disabled, assume players have enough
        }
        return economy.has(player, amount);
    }

    /**
     * Check if an offline player has enough money
     *
     * @param playerId The player's UUID
     * @param amount The amount to check
     * @return true if player has enough, false otherwise
     */
    public boolean has(UUID playerId, double amount) {
        if (!enabled) {
            return true; // If economy is disabled, assume players have enough
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return economy.has(offlinePlayer, amount);
    }

    /**
     * Get a player's balance
     *
     * @param player The player
     * @return The player's balance
     */
    public double getBalance(Player player) {
        if (!enabled) {
            return Double.MAX_VALUE; // If economy is disabled, assume infinite balance
        }
        return economy.getBalance(player);
    }

    /**
     * Get an offline player's balance
     *
     * @param playerId The player's UUID
     * @return The player's balance
     */
    public double getBalance(UUID playerId) {
        if (!enabled) {
            return Double.MAX_VALUE; // If economy is disabled, assume infinite balance
        }
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        return economy.getBalance(offlinePlayer);
    }

    /**
     * Withdraw money from a player
     *
     * @param player The player
     * @param amount The amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdraw(Player player, double amount) {
        if (!enabled) {
            return true; // If economy is disabled, assume withdrawal is successful
        }

        EconomyResponse response = economy.withdrawPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.WARNING, "Failed to withdraw {0} from {1}: {2}",
                    new Object[]{amount, player.getName(), response.errorMessage});
        }

        return response.transactionSuccess();
    }

    /**
     * Withdraw money from an offline player
     *
     * @param playerId The player's UUID
     * @param amount The amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdraw(UUID playerId, double amount) {
        if (!enabled) {
            return true; // If economy is disabled, assume withdrawal is successful
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        EconomyResponse response = economy.withdrawPlayer(offlinePlayer, amount);

        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.WARNING, "Failed to withdraw {0} from {1}: {2}",
                    new Object[]{amount, offlinePlayer.getName(), response.errorMessage});
        }

        return response.transactionSuccess();
    }

    /**
     * Deposit money to a player
     *
     * @param player The player
     * @param amount The amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean deposit(Player player, double amount) {
        if (!enabled) {
            return true; // If economy is disabled, assume deposit is successful
        }

        EconomyResponse response = economy.depositPlayer(player, amount);
        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.WARNING, "Failed to deposit {0} to {1}: {2}",
                    new Object[]{amount, player.getName(), response.errorMessage});
        }

        return response.transactionSuccess();
    }

    /**
     * Deposit money to an offline player
     *
     * @param playerId The player's UUID
     * @param amount The amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean deposit(UUID playerId, double amount) {
        if (!enabled) {
            return true; // If economy is disabled, assume deposit is successful
        }

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerId);
        EconomyResponse response = economy.depositPlayer(offlinePlayer, amount);

        if (!response.transactionSuccess()) {
            plugin.getLogger().log(Level.WARNING, "Failed to deposit {0} to {1}: {2}",
                    new Object[]{amount, offlinePlayer.getName(), response.errorMessage});
        }

        return response.transactionSuccess();
    }
}