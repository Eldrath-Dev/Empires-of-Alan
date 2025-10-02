package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TownKickCommand extends SubCommand {

    public TownKickCommand(EmpiresOfAlan plugin) {
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

        // Check if player has kick permission (Knight+)
        if (!resident.hasTownPermission(TownRole.KNIGHT)) {
            player.sendMessage(configManager.getMessage("towns.not-enough-permissions",
                    "§cYou don't have enough permissions in your town."));
            return false;
        }

        // Get target player
        OfflinePlayer targetPlayer = Bukkit.getPlayerExact(targetName);
        if (targetPlayer == null) {
            // Try to find by name
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                if (offlinePlayer.getName() != null && offlinePlayer.getName().equalsIgnoreCase(targetName)) {
                    targetPlayer = offlinePlayer;
                    break;
                }
            }
        }

        if (targetPlayer == null) {
            player.sendMessage(configManager.getMessage("general.player-not-found",
                    "§cPlayer not found."));
            return false;
        }

        Resident targetResident = residentManager.getResident(targetPlayer.getUniqueId());
        if (targetResident == null) {
            player.sendMessage(configManager.getMessage("residents.not-found",
                    "§cResident not found."));
            return false;
        }

        // Check if target is in the same town
        if (!targetResident.hasTown() || !targetResident.getTownId().equals(resident.getTownId())) {
            player.sendMessage(configManager.getMessage("towns.player-not-in-town",
                    "§cThat player is not in your town."));
            return false;
        }

        // Check if target has higher or equal role
        if (targetResident.getTownRole().getLevel() >= resident.getTownRole().getLevel()) {
            player.sendMessage(configManager.getMessage("towns.cannot-kick-higher-rank",
                    "§cYou cannot kick a player with higher or equal rank."));
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

        // Kick the player
        if (townManager.removeResident(town.getId(), targetPlayer.getUniqueId())) {
            player.sendMessage(configManager.getMessage("towns.player-kicked",
                            "§aPlayer §e{0} §ahas been kicked from your town.")
                    .replace("{0}", targetPlayer.getName()));

            // Notify the target if online
            Player onlineTarget = targetPlayer.getPlayer();
            if (onlineTarget != null && onlineTarget.isOnline()) {
                onlineTarget.sendMessage(configManager.getMessage("towns.you-were-kicked",
                                "§cYou have been kicked from the town §e{0}§c.")
                        .replace("{0}", town.getName()));
            }

            return true;
        } else {
            player.sendMessage(configManager.getMessage("towns.kick-failed",
                    "§cFailed to kick player."));
            return false;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            ResidentManager residentManager = ResidentManager.getInstance();
            Resident resident = residentManager.getResident(player.getUniqueId());

            if (resident != null && resident.hasTown()) {
                String partialName = args[0].toLowerCase();
                Town town = TownManager.getInstance().getTown(resident.getTownId());

                if (town != null) {
                    for (UUID memberId : town.getResidents()) {
                        // Skip the player's own name and the town owner
                        if (memberId.equals(player.getUniqueId()) || memberId.equals(town.getOwnerId())) {
                            continue;
                        }

                        // Skip players with higher or equal rank
                        Resident memberResident = residentManager.getResident(memberId);
                        if (memberResident != null &&
                                memberResident.getTownRole().getLevel() >= resident.getTownRole().getLevel()) {
                            continue;
                        }

                        Player member = Bukkit.getPlayer(memberId);
                        if (member != null) {
                            if (member.getName().toLowerCase().startsWith(partialName)) {
                                completions.add(member.getName());
                            }
                        }
                    }
                }
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Kick a player from your town";
    }

    @Override
    public String getUsage() {
        return "/town kick <player>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.kick";
    }
}