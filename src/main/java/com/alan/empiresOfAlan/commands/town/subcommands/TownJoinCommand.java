package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TownJoinCommand extends SubCommand {

    public TownJoinCommand(EmpiresOfAlan plugin) {
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

        // Check if player is already in a town
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null) {
            resident = residentManager.createResident(player);
        }

        if (resident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.already-in-town",
                    "§cYou are already in a town."));
            return false;
        }

        // Get the town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townName);

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        // Check for invitation
        if (!TownInviteCommand.hasInvitation(player.getUniqueId(), town.getId())) {
            player.sendMessage(configManager.getMessage("towns.not-invited",
                    "§cYou have not been invited to this town."));
            return false;
        }

        // Join the town
        if (townManager.addResident(town.getId(), player.getUniqueId())) {
            // Remove the invitation
            TownInviteCommand.removeInvitation(player.getUniqueId());

            player.sendMessage(configManager.getMessage("towns.joined",
                            "§aYou have joined the town: §e{0}")
                    .replace("{0}", town.getName()));

            return true;
        } else {
            player.sendMessage(configManager.getMessage("towns.join-failed",
                    "§cFailed to join town."));
            return false;
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1 && sender instanceof Player) {
            Player player = (Player) sender;
            String partialName = args[0].toLowerCase();
            TownManager townManager = TownManager.getInstance();

            for (Town town : townManager.getAllTowns().values()) {
                if (TownInviteCommand.hasInvitation(player.getUniqueId(), town.getId())) {
                    if (town.getName().toLowerCase().startsWith(partialName)) {
                        completions.add(town.getName());
                    }
                }
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Join a town you've been invited to";
    }

    @Override
    public String getUsage() {
        return "/town join <town>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.join";
    }
}