package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownDepositCommand extends SubCommand {

    public TownDepositCommand(EmpiresOfAlan plugin) {
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

        // Check if player is in a town
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown()) {
            player.sendMessage(configManager.getMessage("towns.not-in-town",
                    "§cYou are not in a town."));
            return false;
        }

        // TODO: Implement Vault integration to check player balance
        // For now, we'll assume the player has enough money

        // Deposit to town bank
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        // TODO: Implement Vault integration to withdraw from player
        // For now, we'll just add to the town bank

        if (townManager.depositToBank(town.getId(), amount)) {
            player.sendMessage(configManager.getMessage("towns.deposit-success",
                            "§aDeposited §e{0} §ainto the town bank.")
                    .replace("{0}", String.format("%.2f", amount)));
            return true;
        } else {
            player.sendMessage(configManager.getMessage("towns.deposit-failed",
                    "§cFailed to deposit to town bank."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Deposit money into your town's bank";
    }

    @Override
    public String getUsage() {
        return "/town deposit <amount>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.deposit";
    }
}