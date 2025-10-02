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
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NationKickCommand extends SubCommand {

    public NationKickCommand(EmpiresOfAlan plugin) {
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

        // Check if player has kick permission (Officer+)
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

        // Check if town is in the player's nation
        if (!town.hasNation() || !town.getNationId().equals(resident.getNationId())) {
            player.sendMessage(configManager.getMessage("nations.town-not-in-nation",
                    "§cThat town is not in your nation."));
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

        // Check if town is the capital
        if (nation.getCapitalId().equals(town.getId())) {
            player.sendMessage(configManager.getMessage("nations.cannot-kick-capital",
                    "§cYou cannot kick the capital town from the nation."));
            return false;
        }

        // Kick the town
        if (nationManager.removeTown(nation.getId(), town.getId())) {
            player.sendMessage(configManager.getMessage("nations.town-kicked",
                            "§aTown §e{0} §ahas been kicked from your nation.")
                    .replace("{0}", town.getName()));

            // Notify the town owner
            Player townOwner = Bukkit.getPlayer(town.getOwnerId());
            if (townOwner != null && townOwner.isOnline()) {
                townOwner.sendMessage(configManager.getMessage("nations.town-was-kicked",
                                "§cYour town has been kicked from the nation §e{0}§c.")
                        .replace("{0}", nation.getName()));
            }

            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.kick-failed",
                    "§cFailed to kick town from nation."));
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

            if (resident != null && resident.hasNation() && resident.hasNationPermission(NationRole.OFFICER)) {
                String partialName = args[0].toLowerCase();
                Nation nation = NationManager.getInstance().getNation(resident.getNationId());

                if (nation != null) {
                    TownManager townManager = TownManager.getInstance();

                    for (UUID townId : nation.getTowns()) {
                        // Skip the capital town
                        if (townId.equals(nation.getCapitalId())) {
                            continue;
                        }

                        Town town = townManager.getTown(townId);
                        if (town != null) {
                            if (town.getName().toLowerCase().startsWith(partialName)) {
                                completions.add(town.getName());
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
        return "Kick a town from your nation";
    }

    @Override
    public String getUsage() {
        return "/nation kick <town>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.kick";
    }
}