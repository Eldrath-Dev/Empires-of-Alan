package com.alan.empiresOfAlan.commands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand implements CommandExecutor, TabCompleter {
    protected final EmpiresOfAlan plugin;
    protected final ConfigManager configManager;
    protected final Map<String, SubCommand> subCommands;

    public BaseCommand(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
        this.subCommands = new HashMap<>();
        registerSubCommands();
    }

    /**
     * Register all subcommands
     */
    protected abstract void registerSubCommands();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no args, show help
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            sender.sendMessage(configManager.getMessage("general.unknown-command", "§cUnknown command."));
            return true;
        }

        // Check if command requires player
        if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
            sender.sendMessage(configManager.getMessage("general.player-only", "§cThis command can only be used by players."));
            return true;
        }

        // Check permission
        if (!subCommand.hasPermission(sender)) {
            sender.sendMessage(configManager.getMessage("general.no-permission", "§cYou don't have permission to do that."));
            return true;
        }

        // Execute subcommand
        try {
            String[] subArgs = new String[args.length - 1];
            System.arraycopy(args, 1, subArgs, 0, args.length - 1);
            return subCommand.execute(sender, subArgs);
        } catch (Exception e) {
            sender.sendMessage(configManager.getMessage("general.command-error", "§cAn error occurred while executing the command."));
            plugin.getLogger().severe("Error executing command: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Complete subcommand names
            String partialName = args[0].toLowerCase();
            for (String subCommandName : subCommands.keySet()) {
                SubCommand subCommand = subCommands.get(subCommandName);
                if (subCommand.hasPermission(sender) && subCommandName.startsWith(partialName)) {
                    completions.add(subCommandName);
                }
            }
        } else if (args.length > 1) {
            // Pass to subcommand for completion
            SubCommand subCommand = subCommands.get(args[0].toLowerCase());
            if (subCommand != null && subCommand.hasPermission(sender)) {
                String[] subArgs = new String[args.length - 1];
                System.arraycopy(args, 1, subArgs, 0, args.length - 1);
                List<String> subCompletions = subCommand.tabComplete(sender, subArgs);
                if (subCompletions != null) {
                    completions.addAll(subCompletions);
                }
            }
        }

        return completions;
    }

    /**
     * Show command help to a sender
     *
     * @param sender The command sender
     */
    public void showHelp(CommandSender sender) {
        sender.sendMessage("§6==== " + getCommandName() + " Commands ====");
        for (Map.Entry<String, SubCommand> entry : subCommands.entrySet()) {
            SubCommand subCommand = entry.getValue();
            if (subCommand.hasPermission(sender)) {
                sender.sendMessage("§e/" + getCommandName() + " " + entry.getKey() + " §7- " + subCommand.getDescription());
            }
        }
    }

    /**
     * Get the name of this command
     *
     * @return Command name
     */
    public abstract String getCommandName();
}