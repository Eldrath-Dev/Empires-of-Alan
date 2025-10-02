package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class TownSpawnCommand extends SubCommand {

    public TownSpawnCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        ResidentManager residentManager = ResidentManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        // If no args, teleport to own town
        if (args.length == 0) {
            if (resident == null || !resident.hasTown()) {
                player.sendMessage(configManager.getMessage("towns.not-in-town",
                        "§cYou are not in a town."));
                return false;
            }

            Town town = townManager.getTown(resident.getTownId());
            if (town == null || !town.hasSpawn()) {
                player.sendMessage(configManager.getMessage("towns.no-spawn",
                        "§cYour town does not have a spawn point set."));
                return false;
            }

            // Teleport to own town spawn
            teleportToTown(player, town);
            return true;
        }

        // Teleport to specified town
        String townName = args[0];
        Town town = townManager.getTown(townName);

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        if (!town.hasSpawn()) {
            player.sendMessage(configManager.getMessage("towns.no-spawn",
                    "§cThis town does not have a spawn point set."));
            return false;
        }

        // Check if player can teleport to the town
        if (!town.isPublic() && (resident == null || !resident.hasTown() ||
                !resident.getTownId().equals(town.getId()))) {
            player.sendMessage(configManager.getMessage("towns.spawn-private",
                    "§cThis town's spawn is private."));
            return false;
        }

        // Teleport to the town spawn
        teleportToTown(player, town);
        return true;
    }

    private void teleportToTown(Player player, Town town) {
        Location spawnLocation = town.getSpawn();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            player.sendMessage(configManager.getMessage("towns.teleported",
                            "§aTeleported to town: §e{0}")
                    .replace("{0}", town.getName()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            TownManager townManager = TownManager.getInstance();

            for (Town town : townManager.getAllTowns().values()) {
                // Only suggest public towns or towns the player is in
                if (town.isPublic() || (sender instanceof Player &&
                        town.isResident(((Player) sender).getUniqueId()))) {
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
        return "Teleport to a town spawn point";
    }

    @Override
    public String getUsage() {
        return "/town spawn [town]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.spawn";
    }
}