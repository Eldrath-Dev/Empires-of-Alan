package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Pattern;

public class TownCreateCommand extends SubCommand {

    public TownCreateCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        if (args.length < 1) {
            sendUsage(sender);
            return false;
        }

        String townName = args[0];

        // Validate town name
        int minLength = configManager.getConfig().getInt("towns.min-name-length", 3);
        int maxLength = configManager.getConfig().getInt("towns.max-name-length", 16);
        String nameRegex = configManager.getConfig().getString("towns.name-regex", "[a-zA-Z0-9_]+");

        if (townName.length() < minLength || townName.length() > maxLength) {
            player.sendMessage(configManager.getMessage("towns.name-invalid-length",
                            "§cTown name must be between {0} and {1} characters.")
                    .replace("{0}", String.valueOf(minLength))
                    .replace("{1}", String.valueOf(maxLength)));
            return false;
        }

        if (!Pattern.matches(nameRegex, townName)) {
            player.sendMessage(configManager.getMessage("towns.name-invalid-chars",
                    "§cTown name contains invalid characters."));
            return false;
        }

        // Check if town already exists
        TownManager townManager = TownManager.getInstance();
        if (townManager.townExists(townName)) {
            player.sendMessage(configManager.getMessage("towns.already-exists",
                    "§cA town with that name already exists."));
            return false;
        }

        // Create the town (now with event support)
        Town town = townManager.createTown(townName, player);
        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.creation-failed",
                    "§cFailed to create town. You may already be in a town or the event was cancelled."));
            return false;
        }

        player.sendMessage(configManager.getMessage("towns.created",
                        "§aSuccessfully created town: §e{0}")
                .replace("{0}", townName));

        return true;
    }

    @Override
    public String getDescription() {
        return "Create a new town";
    }

    @Override
    public String getUsage() {
        return "/town create <name>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.create";
    }
}