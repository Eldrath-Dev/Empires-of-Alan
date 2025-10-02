package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ClaimManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Chunk;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownClaimCommand extends SubCommand {

    public TownClaimCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            sender.sendMessage(configManager.getMessage("general.player-only", "&cThis command can only be used by players."));
            return false;
        }

        // Check if player is in a town
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.not-in-town",
                    "&cYou are not in a town."));
            return false;
        }

        // Check if player has claim permission (Knight+)
        if (!resident.hasTownPermission(TownRole.KNIGHT)) {
            player.sendMessage(configManager.getMessage("towns.not-enough-permissions",
                    "&cYou don't have enough permissions in your town."));
            return false;
        }

        // Get the town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "&cTown not found."));
            return false;
        }

        // Check if town can claim more chunks
        if (!town.canClaimMore()) {
            player.sendMessage(configManager.getMessage("towns.max-claims-reached",
                    "&cThis town has reached its maximum claim limit."));
            return false;
        }

        // Claim the chunk
        Chunk chunk = player.getLocation().getChunk();
        ClaimManager claimManager = ClaimManager.getInstance();

        if (claimManager.isClaimed(chunk)) {
            player.sendMessage(configManager.getMessage("claims.already-claimed",
                    "&cThis chunk is already claimed."));
            return false;
        }

        Claim claim = claimManager.claimChunk(chunk, town.getId(), player);
        if (claim == null) {
            player.sendMessage(configManager.getMessage("claims.claim-failed",
                    "&cFailed to claim this chunk."));
            return false;
        }

        player.sendMessage(configManager.getMessage("claims.claimed",
                        "&aChunk claimed for town: &e{0}")
                .replace("{0}", town.getName()));

        // Visualize the claim if enabled
        if (configManager.getConfig().getBoolean("claims.visualization.enabled", true)) {
            claimManager.visualizeClaim(chunk);
            int duration = configManager.getConfig().getInt("claims.visualization.duration", 10);
            player.sendMessage(configManager.getMessage("claims.visualize-started",
                            "&aShowing claim borders for &e{0} &aseconds.")
                    .replace("{0}", String.valueOf(duration)));
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Claim the chunk you are standing in for your town";
    }

    @Override
    public String getUsage() {
        return "/town claim";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.claim";
    }
}