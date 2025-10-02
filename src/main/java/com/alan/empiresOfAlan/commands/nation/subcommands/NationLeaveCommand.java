package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationLeaveCommand extends SubCommand {

    public NationLeaveCommand(EmpiresOfAlan plugin) {
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
        if (!resident.hasTownPermission(TownRole.OWNER)) {
            player.sendMessage(configManager.getMessage("towns.not-owner",
                    "§cOnly the town owner can leave a nation."));
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

        // Check if town is in a nation
        if (!town.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation",
                    "§cYour town is not part of a nation."));
            return false;
        }

        // Get the nation
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(town.getNationId());

        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        // Check if town is the capital
        if (nation.getCapitalId().equals(town.getId())) {
            player.sendMessage(configManager.getMessage("nations.cannot-leave-as-capital",
                    "§cYou cannot leave the nation as the capital town. Transfer the capital or delete the nation first."));
            return false;
        }

        // Leave the nation
        if (nationManager.removeTown(nation.getId(), town.getId())) {
            player.sendMessage(configManager.getMessage("nations.town-left",
                            "§aYour town has left the nation: §e{0}")
                    .replace("{0}", nation.getName()));

            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.leave-failed",
                    "§cFailed to leave nation."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Leave your current nation";
    }

    @Override
    public String getUsage() {
        return "/nation leave";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.leave";
    }
}