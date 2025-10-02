package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownDeleteCommand extends SubCommand {

    public TownDeleteCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        // Check if player is in a town
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.not-in-town",
                    "§cYou are not in a town."));
            return false;
        }

        // Check if player is the town owner
        if (!resident.getTownRole().equals(TownRole.OWNER)) {
            player.sendMessage(configManager.getMessage("towns.not-owner",
                    "§cOnly the town owner can delete the town."));
            return false;
        }

        // Get the town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        // Confirm deletion if no args provided
        if (args.length == 0) {
            player.sendMessage(configManager.getMessage("towns.delete-confirm",
                    "§cAre you sure you want to delete your town? This action cannot be undone. Type §e/town delete confirm §cto confirm."));
            return true;
        }

        // Check for confirmation
        if (args.length > 0 && args[0].equalsIgnoreCase("confirm")) {
            // Delete the town
            String townName = town.getName();
            if (townManager.deleteTown(town.getId())) {
                player.sendMessage(configManager.getMessage("towns.deleted",
                                "§aTown §e{0} §ahas been deleted.")
                        .replace("{0}", townName));

                return true;
            } else {
                player.sendMessage(configManager.getMessage("towns.deletion-failed",
                        "§cFailed to delete town."));
                return false;
            }
        } else {
            player.sendMessage(configManager.getMessage("towns.delete-confirm",
                    "§cAre you sure you want to delete your town? This action cannot be undone. Type §e/town delete confirm §cto confirm."));
            return true;
        }
    }

    @Override
    public String getDescription() {
        return "Delete your town";
    }

    @Override
    public String getUsage() {
        return "/town delete [confirm]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.delete";
    }
}