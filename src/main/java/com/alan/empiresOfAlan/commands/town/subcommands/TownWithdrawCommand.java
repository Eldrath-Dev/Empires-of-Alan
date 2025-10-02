package com.alan.empiresOfAlan.commands.town.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TownWithdrawCommand extends SubCommand {

    public TownWithdrawCommand(EmpiresOfAlan plugin) {
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

        // Check if player has withdraw permission (Mayor+)
        if (!resident.hasTownPermission(TownRole.MAYOR)) {
            player.sendMessage(configManager.getMessage("towns.not-enough-permissions",
                    "§cYou don't have enough permissions in your town."));
            return false;
        }

        // Withdraw from town bank
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            player.sendMessage(configManager.getMessage("towns.not-found",
                    "§cTown not found."));
            return false;
        }

        if (town.getBankAccount().getBalance() < amount) {
            player.sendMessage(configManager.getMessage("towns.insufficient-funds",
                    "§cInsufficient funds in the town bank."));
            return false;
        }

        if (townManager.withdrawFromBank(town.getId(), amount)) {
            // TODO: Implement Vault integration to give money to player
            // For now, we'll just remove from the town bank

            player.sendMessage(configManager.getMessage("towns.withdraw-success",
                            "§aWithdrew §e{0} §afrom the town bank.")
                    .replace("{0}", String.format("%.2f", amount)));
            return true;
        } else {
            player.sendMessage(configManager.getMessage("towns.withdraw-failed",
                    "§cFailed to withdraw from town bank."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Withdraw money from your town's bank";
    }

    @Override
    public String getUsage() {
        return "/town withdraw <amount>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.town.withdraw";
    }
}