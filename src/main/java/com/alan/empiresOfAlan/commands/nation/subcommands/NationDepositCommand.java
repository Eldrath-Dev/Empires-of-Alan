package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationDepositCommand extends SubCommand {

    public NationDepositCommand(EmpiresOfAlan plugin) {
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

        // Parse amount
        double amount;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(configManager.getMessage("general.invalid-amount",
                    "§cInvalid amount."));
            return false;
        }

        if (amount <= 0) {
            player.sendMessage(configManager.getMessage("general.invalid-amount",
                    "§cAmount must be positive."));
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

        // TODO: Implement Vault integration to check player balance
        // For now, we'll assume the player has enough money

        // Deposit to nation bank
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(resident.getNationId());

        if (nation == null) {
            player.sendMessage(configManager.getMessage("nations.not-found",
                    "§cNation not found."));
            return false;
        }

        // TODO: Implement Vault integration to withdraw from player
        // For now, we'll just add to the nation bank

        if (nationManager.depositToBank(nation.getId(), amount)) {
            player.sendMessage(configManager.getMessage("nations.deposit-success",
                            "§aDeposited §e{0} §ainto the nation bank.")
                    .replace("{0}", String.format("%.2f", amount)));
            return true;
        } else {
            player.sendMessage(configManager.getMessage("nations.deposit-failed",
                    "§cFailed to deposit to nation bank."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Deposit money into your nation's bank";
    }

    @Override
    public String getUsage() {
        return "/nation deposit <amount>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.deposit";
    }
}