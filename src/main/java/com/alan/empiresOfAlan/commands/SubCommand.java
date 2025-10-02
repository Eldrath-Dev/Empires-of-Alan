package com.alan.empiresOfAlan.commands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public abstract class SubCommand {
    protected final EmpiresOfAlan plugin;
    protected final ConfigManager configManager;

    public SubCommand(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    /**
     * Execute the command
     *
     * @param sender The command sender
     * @param args Command arguments
     * @return true if successful, false otherwise
     */
    public abstract boolean execute(CommandSender sender, String[] args);

    /**
     * Get tab completions for this command
     *
     * @param sender The command sender
     * @param args Command arguments
     * @return List of completions
     */
    public List<String> tabComplete(CommandSender sender, String[] args) {
        return new ArrayList<>();
    }

    /**
     * Get the command description
     *
     * @return Command description
     */
    public abstract String getDescription();

    /**
     * Get the command usage
     *
     * @return Command usage
     */
    public abstract String getUsage();

    /**
     * Check if this command is player-only
     *
     * @return true if player-only, false otherwise
     */
    public boolean isPlayerOnly() {
        return true; // Most commands are player-only
    }

    /**
     * Check if the sender has permission to use this command
     *
     * @param sender The command sender
     * @return true if has permission, false otherwise
     */
    public boolean hasPermission(CommandSender sender) {
        String permission = getPermission();
        return permission == null || sender.hasPermission(permission);
    }

    /**
     * Get the permission node for this command
     *
     * @return Permission node or null if no permission required
     */
    public abstract String getPermission();

    /**
     * Send usage message to the sender
     *
     * @param sender The command sender
     */
    protected void sendUsage(CommandSender sender) {
        sender.sendMessage(configManager.getMessage("general.usage", "&cUsage: {0}").replace("{0}", getUsage()));
    }

    /**
     * Get a player from the command sender
     *
     * @param sender The command sender
     * @return The player or null if sender is not a player
     */
    protected Player getPlayer(CommandSender sender) {
        return (sender instanceof Player) ? (Player) sender : null;
    }
}