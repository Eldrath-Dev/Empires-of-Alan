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

import java.util.regex.Pattern;

public class NationCreateCommand extends SubCommand {

    public NationCreateCommand(EmpiresOfAlan plugin) {
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

        // Validate nation name
        int minLength = configManager.getConfig().getInt("nations.min-name-length", 3);
        int maxLength = configManager.getConfig().getInt("nations.max-name-length", 20);
        String nameRegex = configManager.getConfig().getString("nations.name-regex", "[a-zA-Z0-9_]+");

        if (nationName.length() < minLength || nationName.length() > maxLength) {
            player.sendMessage(configManager.getMessage("nations.name-invalid-length",
                            "§cNation name must be between {0} and {1} characters.")
                    .replace("{0}", String.valueOf(minLength))
                    .replace("{1}", String.valueOf(maxLength)));
            return false;
        }

        if (!Pattern.matches(nameRegex, nationName)) {
            player.sendMessage(configManager.getMessage("nations.name-invalid-chars",
                    "§cNation name contains invalid characters."));
            return false;
        }

        // Check if nation already exists
        NationManager nationManager = NationManager.getInstance();
        if (nationManager.nationExists(nationName)) {
            player.sendMessage(configManager.getMessage("nations.already-exists",
                    "§cA nation with that name already exists."));
            return false;
        }

        // Check if player is a town owner
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown() || !resident.hasTownPermission(TownRole.OWNER)) {
            player.sendMessage(configManager.getMessage("nations.must-be-town-owner",
                    "§cYou must be a town owner to create a nation."));
            return false;
        }

        // Check if town is already in a nation
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        if (town.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.town-already-in-nation",
                    "§cYour town is already part of a nation."));
            return false;
        }

        // Create the nation
        Nation nation = nationManager.createNation(nationName, town.getId(), resident.getUuid());
        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.creation-failed",
                    "§cFailed to create nation."));
            return false;
        }

        player.sendMessage(configManager.getMessage("nations.created",
                        "§aSuccessfully created nation: §e{0}")
                .replace("{0}", nationName));

        return true;
    }

    @Override
    public String getDescription() {
        return "Create a new nation with your town as the capital";
    }

    @Override
    public String getUsage() {
        return "/nation create <name>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.create";
    }
}