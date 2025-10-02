package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ClaimManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownUnclaimCommand extends SubCommand {

    public TownUnclaimCommand(EmpiresOfAlan plugin) {
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

        // Check if player has unclaim permission (Knight+)
        if (!resident.hasTownPermission(TownRole.KNIGHT)) {
            player.sendMessage(configManager.getMessage("towns.not-enough-permissions",
                    "§cYou don't have enough permissions in your town."));
            return false;
        }

        // Unclaim the chunk
        Chunk chunk = player.getLocation().getChunk();
        ClaimManager claimManager = ClaimManager.getInstance();

        Claim claim = claimManager.getClaimAt(chunk);
        if (claim == null) {
            player.sendMessage(configManager.getMessage("claims.not-claimed",
                    "§cThis chunk is not claimed."));
            return false;
        }

        // Check if the claim belongs to player's town
        if (!claim.getTownId().equals(resident.getTownId())) {
            player.sendMessage(configManager.getMessage("claims.not-owned",
                    "§cThis chunk is not owned by your town."));
            return false;
        }

        if (claimManager.unclaimChunk(chunk, player)) {
            player.sendMessage(configManager.getMessage("claims.unclaimed",
                    "§aChunk unclaimed."));
            return true;
        } else {
            player.sendMessage(configManager.getMessage("claims.unclaim-failed",
                    "§cFailed to unclaim this chunk."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Unclaim the chunk you are standing in";
    }

    @Override
    public String getUsage() {
        return "/town unclaim";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.unclaim";
    }
}