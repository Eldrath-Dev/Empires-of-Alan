package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class NationListCommand extends SubCommand {

    public NationListCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        int page = 1;

        // Parse page number if provided
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) page = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(configManager.getMessage("general.invalid-page", "&cInvalid page number."));
                return false;
            }
        }

        // Get all nations
        NationManager nationManager = NationManager.getInstance();
        Map<UUID, Nation> allNations = nationManager.getAllNations();

        if (allNations.isEmpty()) {
            sender.sendMessage(configManager.getMessage("nations.no-nations", "&cThere are no nations yet."));
            return true;
        }

        // Convert to list for pagination
        List<Nation> nationList = new ArrayList<>(allNations.values());

        // Sort nations by name
        nationList.sort((n1, n2) -> n1.getName().compareToIgnoreCase(n2.getName()));

        // Calculate pagination
        int nationsPerPage = configManager.getConfig().getInt("list.nations-per-page", 10);
        int totalPages = (int) Math.ceil((double) nationList.size() / nationsPerPage);

        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * nationsPerPage;
        int endIndex = Math.min(startIndex + nationsPerPage, nationList.size());

        // Send header
        sender.sendMessage(configManager.getMessage("nations.list-header",
                        "&6---- Nations (Page {0}/{1}) ----")
                .replace("{0}", String.valueOf(page))
                .replace("{1}", String.valueOf(totalPages)));

        // Send nation list for this page
        ResidentManager residentManager = ResidentManager.getInstance();

        for (int i = startIndex; i < endIndex; i++) {
            Nation nation = nationList.get(i);
            Resident leader = residentManager.getResident(nation.getLeaderId());
            String leaderName = leader != null ? leader.getName() : "Unknown";

            sender.sendMessage(configManager.getMessage("nations.list-entry",
                            "&e{0} &7(Leader: &f{1}&7, &f{2} &7towns)")
                    .replace("{0}", nation.getName())
                    .replace("{1}", leaderName)
                    .replace("{2}", String.valueOf(nation.getTownCount())));
        }

        // Send footer with navigation if needed
        if (totalPages > 1) {
            String footer = "&6";
            if (page > 1) {
                footer += "<<< Page " + (page - 1) + " ";
            }
            footer += "&7(Page " + page + " of " + totalPages + ")";
            if (page < totalPages) {
                footer += " &6Page " + (page + 1) + " >>>";
            }
            sender.sendMessage(footer);
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "List all nations";
    }

    @Override
    public String getUsage() {
        return "/nation list [page]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.list";
    }

    @Override
    public boolean isPlayerOnly() {
        return false; // Can be used by console
    }
}