package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Resident;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationChatCommand extends SubCommand {

    public NationChatCommand(EmpiresOfAlan plugin) {
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

        if (resident == null || !resident.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation",
                    "§cYou are not in a nation."));
            return false;
        }

        // Toggle nation chat
        boolean chatEnabled = residentManager.toggleNationChat(resident.getUuid());

        if (chatEnabled) {
            player.sendMessage(configManager.getMessage("nations.chat-enabled",
                    "§aNation chat enabled."));
        } else {
            player.sendMessage(configManager.getMessage("nations.chat-disabled",
                    "§aNation chat disabled."));
        }

        return true;
    }

    @Override
    public String getDescription() {
        return "Toggle nation chat mode";
    }

    @Override
    public String getUsage() {
        return "/nation chat";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.chat";
    }
}