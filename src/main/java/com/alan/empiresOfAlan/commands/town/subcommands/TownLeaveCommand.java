package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownLeaveCommand extends SubCommand {

    public TownLeaveCommand(EmpiresOfAlan plugin) {
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

        // Get the town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        // Check if player is the owner
        if (town.getOwnerId().equals(player.getUniqueId())) {
            player.sendMessage(configManager.getMessage("towns.cannot-leave-as-owner",
                    "§cYou cannot leave your town as the owner. Transfer ownership or delete the town first."));
            return false;
        }

        // Leave the town
        if (townManager.removeResident(town.getId(), player.getUniqueId())) {
            player.sendMessage(configManager.getMessage("towns.left",
                            "§aYou have left the town: §e{0}")
                    .replace("{0}", town.getName()));

            return true;
        } else {
            player.sendMessage(configManager.getMessage("towns.leave-failed",
                    "§cFailed to leave town."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Leave your current town";
    }

    @Override
    public String getUsage() {
        return "/town leave";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.leave";
    }
}