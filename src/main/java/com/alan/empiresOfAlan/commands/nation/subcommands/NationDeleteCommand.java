package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationDeleteCommand extends SubCommand {

    public NationDeleteCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        Player player = getPlayer(sender);
        if (player == null) {
            return false;
        }

        // Check if player is in a nation
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasNation()) {
            player.sendMessage(configManager.getMessage("nations.not-in-nation",
                    "§cYou are not in a nation."));
            return false;
        }

        // Check if player is the king
        if (!resident.getNationRole().equals(NationRole.KING)) {
            player.sendMessage(configManager.getMessage("nations.not-king",
                    "§cOnly the king can delete the nation."));
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

        // Delete the nation
        String nationName = nation.getName();
        if (nationManager.deleteNation(nation.getId())) {
            player.sendMessage(configManager.getMessage("nations.deleted",
                            "§aNation §e{0} §ahas been deleted.")
                    .replace("{0}", nationName));

            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.deletion-failed",
                    "§cFailed to delete nation."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Delete your nation";
    }

    @Override
    public String getUsage() {
        return "/nation delete";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.delete";
    }
}