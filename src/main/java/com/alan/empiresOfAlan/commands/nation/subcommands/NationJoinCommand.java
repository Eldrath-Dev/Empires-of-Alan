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

import java.util.ArrayList;
import java.util.List;

public class NationJoinCommand extends SubCommand {

    public NationJoinCommand(EmpiresOfAlan plugin) {
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

        String nationName = args[0];

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
                    "§cOnly the town owner can join a nation."));
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

        // Check if town is already in a nation
        if (town.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.town-already-in-nation",
                    "§cYour town is already part of a nation."));
            return false;
        }

        // Get the nation
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(nationName);

        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        // Check for invitation
        if (!NationInviteCommand.hasInvitation(town.getId(), nation.getId())) {
            player.sendMessage(configManager.getMessage("nations.not-invited",
                    "§cYour town has not been invited to this nation."));
            return false;
        }

        // Join the nation
        if (nationManager.addTown(nation.getId(), town.getId())) {
            // Remove the invitation
            NationInviteCommand.removeInvitation(town.getId());

            player.sendMessage(configManager.getMessage("nations.town-joined",
                            "§aYour town has joined the nation: §e{0}")
                    .replace("{0}", nation.getName()));

            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.join-failed",
                    "§cFailed to join nation."));
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

            if (resident != null && resident.hasTown() && resident.hasTownPermission(TownRole.OWNER)) {
                Town town = TownManager.getInstance().getTown(resident.getTownId());

                if (town != null && !town.hasNation()) {
                    String partialName = args[0].toLowerCase();

                    for (Nation nation : NationManager.getInstance().getAllNations().values()) {
                        if (NationInviteCommand.hasInvitation(town.getId(), nation.getId())) {
                            if (nation.getName().toLowerCase().startsWith(partialName)) {
                                completions.add(nation.getName());
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
        return "Join a nation your town has been invited to";
    }

    @Override
    public String getUsage() {
        return "/nation join <nation>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.join";
    }
}