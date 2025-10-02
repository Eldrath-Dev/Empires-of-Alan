package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class TownInfoCommand extends SubCommand {

    public TownInfoCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // If no args, show own town
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getMessage("general.player-only",
                        "§cThis command can only be used by players."));
                return false;
            }

            Player player = (Player) sender;
            ResidentManager residentManager = ResidentManager.getInstance();
            Resident resident = residentManager.getResident(player.getUniqueId());

            if (resident == null || !resident.hasTown()) {
                player.sendMessage(configManager.getMessage("towns.not-in-town",
                        "§cYou are not in a town."));
                return false;
            }

            TownManager townManager = TownManager.getInstance();
            Town town = townManager.getTown(resident.getTownId());

            if (town == null) {
                player.sendMessage(configManager.getMessage("towns.not-found",
                        "§cTown not found."));
                return false;
            }

            showTownInfo(sender, town);
            return true;
        }

        // Show specified town
        String townName = args[0];
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townName);

        if (town == null) {
            sender.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        showTownInfo(sender, town);
        return true;
    }

    private void showTownInfo(CommandSender sender, Town town) {
        ResidentManager residentManager = ResidentManager.getInstance();
        NationManager nationManager = NationManager.getInstance();

        OfflinePlayer owner = Bukkit.getOfflinePlayer(town.getOwnerId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        sender.sendMessage("§6=== Town: §e" + town.getName() + " §6===");
        sender.sendMessage("§6Owner: §e" + (owner.getName() != null ? owner.getName() : "Unknown"));

        // Show nation if part of one
        if (town.hasNation()) {
            Nation nation = nationManager.getNation(town.getNationId());
            if (nation != null) {
                sender.sendMessage("§6Nation: §e" + nation.getName());
            }
        }

        // Show bank balance
        sender.sendMessage("§6Bank: §e" + String.format("%.2f", town.getBankAccount().getBalance()));

        // Show claim count
        sender.sendMessage("§6Claims: §e" + town.getClaimCount() + "/" + town.getMaxClaims());

        // Show tax info
        sender.sendMessage("§6Tax Rate: §e" + town.getTaxRate() + "%");
        sender.sendMessage("§6Last Tax Collection: §e" + dateFormat.format(new Date(town.getLastTaxCollection())));

        // Show residents by role
        List<String> owners = new ArrayList<>();
        List<String> mayors = new ArrayList<>();
        List<String> knights = new ArrayList<>();
        List<String> members = new ArrayList<>();

        for (UUID residentId : town.getResidents()) {
            Resident resident = residentManager.getResident(residentId);
            if (resident == null) continue;

            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(residentId);
            String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : "Unknown";

            switch (resident.getTownRole()) {
                case OWNER:
                    owners.add(name);
                    break;
                case MAYOR:
                    mayors.add(name);
                    break;
                case KNIGHT:
                    knights.add(name);
                    break;
                case MEMBER:
                    members.add(name);
                    break;
            }
        }

        if (!owners.isEmpty()) {
            sender.sendMessage("§6Owners: §e" + String.join(", ", owners));
        }

        if (!mayors.isEmpty()) {
            sender.sendMessage("§6Mayors: §e" + String.join(", ", mayors));
        }

        if (!knights.isEmpty()) {
            sender.sendMessage("§6Knights: §e" + String.join(", ", knights));
        }

        if (!members.isEmpty()) {
            sender.sendMessage("§6Members: §e" + String.join(", ", members));
        }

        // Show spawn status
        sender.sendMessage("§6Spawn: §e" + (town.hasSpawn() ? "Set" : "Not Set"));
        sender.sendMessage("§6Public: §e" + (town.isPublic() ? "Yes" : "No"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            TownManager townManager = TownManager.getInstance();

            for (Town town : townManager.getAllTowns().values()) {
                if (town.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(town.getName());
                }
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Show information about a town";
    }

    @Override
    public String getUsage() {
        return "/town info [town]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.info";
    }

    @Override
    public boolean isPlayerOnly() {
        return false; // Allow console to use this command
    }
}