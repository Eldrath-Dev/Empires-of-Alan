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
import java.util.Map;
import java.util.UUID;

public class TownListCommand extends SubCommand {

    public TownListCommand(EmpiresOfAlan plugin) {
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

        // Get all towns
        TownManager townManager = TownManager.getInstance();
        Map<UUID, Town> allTowns = townManager.getAllTowns();

        if (allTowns.isEmpty()) {
            sender.sendMessage(configManager.getMessage("towns.no-towns", "&cThere are no towns yet."));
            return true;
        }

        // Convert to list for pagination
        List<Town> townList = new ArrayList<>(allTowns.values());

        // Sort towns by name
        townList.sort((t1, t2) -> t1.getName().compareToIgnoreCase(t2.getName()));

        // Calculate pagination
        int townsPerPage = configManager.getConfig().getInt("list.towns-per-page", 10);
        int totalPages = (int) Math.ceil((double) townList.size() / townsPerPage);

        if (page > totalPages) page = totalPages;

        int startIndex = (page - 1) * townsPerPage;
        int endIndex = Math.min(startIndex + townsPerPage, townList.size());

        // Send header
        sender.sendMessage(configManager.getMessage("towns.list-header",
                        "&6---- Towns (Page {0}/{1}) ----")
                .replace("{0}", String.valueOf(page))
                .replace("{1}", String.valueOf(totalPages)));

        // Send town list for this page
        ResidentManager residentManager = ResidentManager.getInstance();

        for (int i = startIndex; i < endIndex; i++) {
            Town town = townList.get(i);
            Resident mayor = residentManager.getResident(town.getOwnerId());
            String mayorName = mayor != null ? mayor.getName() : "Unknown";

            sender.sendMessage(configManager.getMessage("towns.list-entry",
                            "&e{0} &7(Mayor: &f{1}&7, &f{2} &7residents)")
                    .replace("{0}", town.getName())
                    .replace("{1}", mayorName)
                    .replace("{2}", String.valueOf(town.getResidentCount())));
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
        return "List all towns";
    }

    @Override
    public String getUsage() {
        return "/town list [page]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.list";
    }

    @Override
    public boolean isPlayerOnly() {
        return false; // Can be used by console
    }
}