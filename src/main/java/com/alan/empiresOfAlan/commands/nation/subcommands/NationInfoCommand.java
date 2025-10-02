package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NationInfoCommand extends SubCommand {

    public NationInfoCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        // If no args, show own nation
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(configManager.getMessage("general.player-only",
                        "§cThis command can only be used by players."));
                return false;
            }

            Player player = (Player) sender;
            ResidentManager residentManager = ResidentManager.getInstance();
            Resident resident = residentManager.getResident(player.getUniqueId());

            if (resident == null || !resident.hasNation()) {
                player.sendMessage(configManager.getMessage("nations.not-in-nation",
                        "§cYou are not in a nation."));
                return false;
            }

            NationManager nationManager = NationManager.getInstance();
            Nation nation = nationManager.getNation(resident.getNationId());

            if (nation == null) {
                player.sendMessage(configManager.getMessage("nations.not-found",
                        "§cNation not found."));
                return false;
            }

            showNationInfo(sender, nation);
            return true;
        }

        // Show specified nation
        String nationName = args[0];
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(nationName);

        if (nation == null) {
            sender.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        showNationInfo(sender, nation);
        return true;
    }

    private void showNationInfo(CommandSender sender, Nation nation) {
        ResidentManager residentManager = ResidentManager.getInstance();
        TownManager townManager = TownManager.getInstance();

        OfflinePlayer leader = Bukkit.getOfflinePlayer(nation.getLeaderId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        sender.sendMessage("§6=== Nation: §e" + nation.getName() + " §6===");
        sender.sendMessage("§6Leader: §e" + (leader.getName() != null ? leader.getName() : "Unknown"));

        // Show capital town
        Town capital = townManager.getTown(nation.getCapitalId());
        if (capital != null) {
            sender.sendMessage("§6Capital: §e" + capital.getName());
        }

        // Show bank balance
        sender.sendMessage("§6Bank: §e" + String.format("%.2f", nation.getBankAccount().getBalance()));

        // Show town count
        sender.sendMessage("§6Towns: §e" + nation.getTownCount());

        // Show tax info
        sender.sendMessage("§6Tax Rate: §e" + nation.getTaxRate() + "%");
        sender.sendMessage("§6Last Tax Collection: §e" + dateFormat.format(new Date(nation.getLastTaxCollection())));

        // Show towns
        List<String> townNames = new ArrayList<>();
        for (UUID townId : nation.getTowns()) {
            Town town = townManager.getTown(townId);
            if (town != null) {
                townNames.add(town.getName());
            }
        }

        if (!townNames.isEmpty()) {
            sender.sendMessage("§6Towns: §e" + String.join(", ", townNames));
        }

        // Show spawn status
        sender.sendMessage("§6Spawn: §e" + (nation.hasSpawn() ? "Set" : "Not Set"));
        sender.sendMessage("§6Public: §e" + (nation.isPublic() ? "Yes" : "No"));
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            NationManager nationManager = NationManager.getInstance();

            for (Nation nation : nationManager.getAllNations().values()) {
                if (nation.getName().toLowerCase().startsWith(partialName)) {
                    completions.add(nation.getName());
                }
            }
        }

        return completions;
    }

    @Override
    public String getDescription() {
        return "Show information about a nation";
    }

    @Override
    public String getUsage() {
        return "/nation info [nation]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.info";
    }

    @Override
    public boolean isPlayerOnly() {
        return false; // Allow console to use this command
    }
}