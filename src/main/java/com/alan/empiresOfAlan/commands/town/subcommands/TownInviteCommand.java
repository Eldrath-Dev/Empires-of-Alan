package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TownInviteCommand extends SubCommand {
    // Store pending invitations: key = player UUID, value = town UUID
    private static final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public TownInviteCommand(EmpiresOfAlan plugin) {
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

        String targetName = args[0];

        // Check if player is in a town
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.not-in-town",
                    "§cYou are not in a town."));
            return false;
        }

        // Check if player has invite permission (Knight+)
        if (!resident.hasTownPermission(TownRole.KNIGHT)) {
            player.sendMessage(configManager.getMessage("towns.not-enough-permissions",
                    "§cYou don't have enough permissions in your town."));
            return false;
        }

        // Get target player
        Player targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer == null) {
            player.sendMessage(configManager.getMessage("general.player-not-found",
                    "§cPlayer not found or not online."));
            return false;
        }

        Resident targetResident = residentManager.getResident(targetPlayer.getUniqueId());
        if (targetResident == null) {
            targetResident = residentManager.createResident(targetPlayer);
        }

        // Check if target is already in a town
        if (targetResident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.player-already-in-town",
                    "§cThat player is already in a town."));
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

        // Send the invitation
        pendingInvites.put(targetPlayer.getUniqueId(), town.getId());

        player.sendMessage(configManager.getMessage("towns.invite-sent",
                        "§aInvitation sent to §e{0}")
                .replace("{0}", targetPlayer.getName()));

        targetPlayer.sendMessage(configManager.getMessage("towns.invited",
                        "§aYou have been invited to join the town §e{0}§a. Type §e/town join {0} §ato accept.")
                .replace("{0}", town.getName()));

        return true;
    }

    /**
     * Check if a player has a pending invitation to a town
     *
     * @param playerId Player UUID
     * @param townId Town UUID
     * @return true if invitation exists
     */
    public static boolean hasInvitation(UUID playerId, UUID townId) {
        UUID invitedTownId = pendingInvites.get(playerId);
        return invitedTownId != null && invitedTownId.equals(townId);
    }

    /**
     * Remove a pending invitation
     *
     * @param playerId Player UUID
     */
    public static void removeInvitation(UUID playerId) {
        pendingInvites.remove(playerId);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(onlinePlayer.getName());
                }
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Invite a player to your town";
    }

    @Override
    public String getUsage() {
        return "/town invite <player>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.invite";
    }
}