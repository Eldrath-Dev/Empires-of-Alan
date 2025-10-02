package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Resident;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownChatCommand extends SubCommand {

    public TownChatCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.not-in-town",
                    "§cYou are not in a town."));
            return false;
        }

        // Toggle town chat
        boolean chatEnabled = residentManager.toggleTownChat(resident.getUuid());

        if (chatEnabled) {
            player.sendMessage(configManager.getMessage("towns.chat-enabled",
                    "§aTown chat enabled."));
        } else {
            player.sendMessage(configManager.getMessage("towns.chat-disabled",
                    "§aTown chat disabled."));
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Toggle town chat mode";
    }

    @Override
    public String getUsage() {
        return "/town chat";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.chat";
    }
}