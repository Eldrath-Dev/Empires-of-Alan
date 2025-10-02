package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NationSpawnCommand extends SubCommand {

    public NationSpawnCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        ResidentManager residentManager = ResidentManager.getInstance();
        NationManager nationManager = NationManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        // If no args, teleport to own nation
        if (args.length == 0) {
            if (resident == null || !resident.hasNation()) {
                player.sendMessage(configManager.getMessage("nations.not-in-nation",
                        "§cYou are not in a nation."));
                return false;
            }

            Nation nation = nationManager.getNation(resident.getNationId());
            if (nation == null || !nation.hasSpawn()) {
                player.sendMessage(configManager.getMessage("nations.no-spawn",
                        "§cYour nation does not have a spawn point set."));
                return false;
            }

            // Teleport to own nation spawn
            teleportToNation(player, nation);
            return true;
        }

        // Teleport to specified nation
        String nationName = args[0];
        Nation nation = nationManager.getNation(nationName);

        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        if (!nation.hasSpawn()) {
            player.sendMessage(configManager.getMessage("nations.no-spawn",
                    "§cThis nation does not have a spawn point set."));
            return false;
        }

        // Check if player can teleport to the nation
        if (!nation.isPublic() && (resident == null || !resident.hasNation() ||
                !resident.getNationId().equals(nation.getId()))) {
            player.sendMessage(configManager.getMessage("nations.spawn-private",
                    "§cThis nation's spawn is private."));
            return false;
        }

        // Teleport to the nation spawn
        teleportToNation(player, nation);
        return true;
    }

    private void teleportToNation(Player player, Nation nation) {
        Location spawnLocation = nation.getSpawn();
        if (spawnLocation != null) {
            player.teleport(spawnLocation);
            player.sendMessage(configManager.getMessage("nations.teleported",
                            "§aTeleported to nation: §e{0}")
                    .replace("{0}", nation.getName()));
        }
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String partialName = args[0].toLowerCase();
            NationManager nationManager = NationManager.getInstance();

            for (Nation nation : nationManager.getAllNations().values()) {
                // Only suggest public nations or nations the player is in
                if (nation.isPublic() || (sender instanceof Player &&
                        isInNation((Player) sender, nation.getId()))) {
                    if (nation.getName().toLowerCase().startsWith(partialName)) {
                        completions.add(nation.getName());
                    }
                }
            }
        }

        return completions;
    }

    private boolean isInNation(Player player, UUID nationId) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());
        return resident != null && resident.hasNation() && resident.getNationId().equals(nationId);
    }

    @Override
    public String getDescription() {
        return "Teleport to a nation spawn point";
    }

    @Override
    public String getUsage() {
        return "/nation spawn [nation]";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.spawn";
    }
}