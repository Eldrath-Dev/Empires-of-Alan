package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ClaimManager;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NationSetSpawnCommand extends SubCommand {

    public NationSetSpawnCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        // Check if player is in a nation
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation",
                    "§cYou are not in a nation."));
            return false;
        }

        // Check if player has permission (Officer+)
        if (!resident.hasNationPermission(NationRole.OFFICER)) {
            player.sendMessage(configManager.getMessage("nations.not-enough-permissions",
                    "§cYou don't have enough permissions in your nation."));
            return false;
        }

        // Check if player is in a town that belongs to their nation
        ClaimManager claimManager = ClaimManager.getInstance();
        Location location = player.getLocation();
        UUID townId = claimManager.getTownAt(location.getChunk());

        if (townId == null) {
            player.sendMessage(configManager.getMessage("nations.not-in-claim",
                    "§cYou must be in a town's territory to set the nation spawn."));
            return false;
        }

        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townId);

        if (town == null || !town.hasNation() || !town.getNationId().equals(resident.getNationId())) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation-territory",
                    "§cYou must be in your nation's territory to set the spawn."));
            return false;
        }

        // Set spawn
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(resident.getNationId());

        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        if (nationManager.setSpawn(nation.getId(), location)) {
            player.sendMessage(configManager.getMessage("nations.spawn-set",
                    "§aNation spawn has been set."));
            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.spawn-set-failed",
                    "§cFailed to set nation spawn."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Set your nation's spawn point to your current location";
    }

    @Override
    public String getUsage() {
        return "/nation setspawn";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.setspawn";
    }
}