package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NationPromoteCommand extends SubCommand {

    public NationPromoteCommand(EmpiresOfAlan plugin) {
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

        // Check if player is in a nation
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation",
                    "§cYou are not in a nation."));
            return false;
        }

        // Check if player has promote permission (Officer+)
        if (!resident.hasNationPermission(NationRole.OFFICER)) {
            player.sendMessage(configManager.getMessage("nations.not-enough-permissions",
                    "§cYou don't have enough permissions in your nation."));
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

        // Check if target is in the same nation
        if (!targetResident.hasNation() || !targetResident.getNationId().equals(resident.getNationId())) {
            player.sendMessage(configManager.getMessage("nations.player-not-in-nation",
                    "§cThat player is not in your nation."));
            return false;
        }

        // Promote the player
        NationManager nationManager = NationManager.getInstance();
        if (nationManager.promoteResident(player.getUniqueId(), targetPlayer.getUniqueId())) {
            // Get the new role
            NationRole newRole = targetResident.getNationRole();

            player.sendMessage(configManager.getMessage("nations.promoted",
                            "§aPlayer §e{0} §ahas been promoted to §e{1}")
                    .replace("{0}", targetPlayer.getName())
                    .replace("{1}", newRole.getDisplayName()));

            // Notify the target if online
            Player onlineTarget = targetPlayer.getPlayer();
            if (onlineTarget != null && onlineTarget.isOnline()) {
                onlineTarget.sendMessage(configManager.getMessage("nations.you-were-promoted",
                                "§aYou have been promoted to §e{0} §ain your nation.")
                        .replace("{0}", newRole.getDisplayName()));
            }

            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.promotion-failed",
                    "§cFailed to promote player. They may already be at the highest rank."));
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

            if (resident != null && resident.hasNation()) {
                String partialName = args[0].toLowerCase();
                Nation nation = NationManager.getInstance().getNation(resident.getNationId());

                if (nation != null) {
                    TownManager townManager = TownManager.getInstance();

                    // Iterate through all towns in the nation
                    for (UUID townId : nation.getTowns()) {
                        Town town = townManager.getTown(townId);
                        if (town != null) {
                            // Iterate through all residents in the town
                            for (UUID memberId : town.getResidents()) {
                                // Skip the player's own name and the nation leader
                                if (memberId.equals(player.getUniqueId()) || memberId.equals(nation.getLeaderId())) {
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
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Promote a player in your nation";
    }

    @Override
    public String getUsage() {
        return "/nation promote <player>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.promote";
    }
}