package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ClaimManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownSetSpawnCommand extends SubCommand {

    public TownSetSpawnCommand(EmpiresOfAlan plugin) {
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

        // Check if player has permission (Mayor+)
        if (!resident.hasTownPermission(TownRole.MAYOR)) {
            player.sendMessage(configManager.getMessage("towns.not-enough-permissions",
                    "§cYou don't have enough permissions in your town."));
            return false;
        }

        // Check if player is in their town's claim
        ClaimManager claimManager = ClaimManager.getInstance();
        Location location = player.getLocation();

        if (!claimManager.isTownClaim(location.getChunk(), resident.getTownId())) {
            player.sendMessage(configManager.getMessage("towns.not-in-claim",
                    "§cYou must be in your town's territory to set the spawn."));
            return false;
        }

        // Set spawn
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        if (townManager.setSpawn(town.getId(), location)) {
            player.sendMessage(configManager.getMessage("towns.spawn-set",
                    "§aTown spawn has been set."));
            return true;
        } else {
            player.sendMessage(configManager.getMessage("towns.spawn-set-failed",
                    "§cFailed to set town spawn."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Set your town's spawn point to your current location";
    }

    @Override
    public String getUsage() {
        return "/town setspawn";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.setspawn";
    }
}