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
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NationInviteCommand extends SubCommand {
    // Store pending invitations: key = town UUID, value = nation UUID
    private static final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public NationInviteCommand(EmpiresOfAlan plugin) {
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

        String townName = args[0];

        // Check if player is in a nation
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation",
                    "§cYou are not in a nation."));
            return false;
        }

        // Check if player has invite permission (Officer+)
        if (!resident.hasNationPermission(NationRole.OFFICER)) {
            player.sendMessage(configManager.getMessage("nations.not-enough-permissions",
                    "§cYou don't have enough permissions in your nation."));
            return false;
        }

        // Get the target town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townName);

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        // Check if town is already in a nation
        if (town.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.town-already-in-nation",
                    "§cThat town is already part of a nation."));
            return false;
        }

        // Get the nation
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(resident.getNationId());

        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        // Send the invitation
        pendingInvites.put(town.getId(), nation.getId());

        player.sendMessage(configManager.getMessage("nations.invite-sent",
                        "§aInvitation sent to town §e{0}")
                .replace("{0}", town.getName()));

        // Notify the town owner
        Player townOwner = Bukkit.getPlayer(town.getOwnerId());
        if (townOwner != null && townOwner.isOnline()) {
            townOwner.sendMessage(configManager.getMessage("nations.town-invited",
                            "§aYour town has been invited to join the nation §e{0}§a. Type §e/nation join {0} §ato accept.")
                    .replace("{0}", nation.getName()));
        }

        return true;
    }

    /**
     * Check if a town has a pending invitation to a nation
     *
     * @param townId Town UUID
     * @param nationId Nation UUID
     * @return true if invitation exists
     */
    public static boolean hasInvitation(UUID townId, UUID nationId) {
        UUID invitedNationId = pendingInvites.get(townId);
        return invitedNationId != null && invitedNationId.equals(nationId);
    }

    /**
     * Remove a pending invitation
     *
     * @param townId Town UUID
     */
    public static void removeInvitation(UUID townId) {
        pendingInvites.remove(townId);
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
                TownManager townManager = TownManager.getInstance();

                for (Town town : townManager.getAllTowns().values()) {
                    // Only suggest towns not in a nation
                    if (!town.hasNation()) {
                        if (town.getName().toLowerCase().startsWith(partialName)) {
                            completions.add(town.getName());
                        }
                    }
                }
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Invite a town to your nation";
    }

    @Override
    public String getUsage() {
        return "/nation invite <town>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.invite";
    }
}