package com.alan.empiresOfAlan.commands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.nation.NationCommand;
import com.alan.empiresOfAlan.commands.town.TownCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public class CommandManager {
    private final EmpiresOfAlan plugin;
    private TownCommand townCommand;
    private NationCommand nationCommand;

    public CommandManager(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        registerCommands();
    }

    private void registerCommands() {
        // Register town commands
        townCommand = new TownCommand(plugin);
        PluginCommand townCmd = plugin.getCommand("town");
        if (townCmd != null) {
            townCmd.setExecutor((CommandExecutor) townCommand);
            townCmd.setTabCompleter((TabCompleter) townCommand);
        }

        // Register nation commands
        nationCommand = new NationCommand(plugin);
        PluginCommand nationCmd = plugin.getCommand("nation");
        if (nationCmd != null) {
            nationCmd.setExecutor((CommandExecutor) nationCommand);
            nationCmd.setTabCompleter((TabCompleter) nationCommand);
        }

        // Aliases
        PluginCommand tCmd = plugin.getCommand("t");
        if (tCmd != null) {
            tCmd.setExecutor((CommandExecutor) townCommand);
            tCmd.setTabCompleter((TabCompleter) townCommand);
        }

        PluginCommand nCmd = plugin.getCommand("n");
        if (nCmd != null) {
            nCmd.setExecutor((CommandExecutor) nationCommand);
            nCmd.setTabCompleter((TabCompleter) nationCommand);
        }

        plugin.getLogger().info("Commands registered successfully!");
    }
}