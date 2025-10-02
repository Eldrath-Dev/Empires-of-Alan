package com.alan.empiresOfAlan.commands.nation.subcommands;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.SubCommand;
import com.alan.empiresOfAlan.integrations.VaultIntegration;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NationWithdrawCommand extends SubCommand {

    public NationWithdrawCommand(EmpiresOfAlan plugin) {
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

        // Check if player has withdraw permission (Officer+)
        if (!resident.hasNationPermission(NationRole.OFFICER)) {
            player.sendMessage(configManager.getMessage("nations.not-enough-permissions",
                    "§cYou don't have enough permissions in your nation."));
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

        // Check if nation has enough money
        if (nation.getBankAccount().getBalance() < amount) {
            player.sendMessage(configManager.getMessage("nations.insufficient-funds",
                    "§cInsufficient funds in the nation bank."));
            return false;
        }

        // Get Vault integration
        VaultIntegration vaultIntegration = plugin.getVaultIntegration();

        // Withdraw from nation bank and deposit to player
        if (nationManager.withdrawFromBank(nation.getId(), amount)) {
            if (vaultIntegration.deposit(player, amount)) {
                player.sendMessage(configManager.getMessage("nations.withdraw-success",
                                "§aWithdrew §e{0} §afrom the nation bank.")
                        .replace("{0}", vaultIntegration.format(amount)));
                return true;
            } else {
                // Refund the nation bank if player deposit failed
                nationManager.depositToBank(nation.getId(), amount);
                player.sendMessage(configManager.getMessage("nations.withdraw-failed",
                        "§cFailed to withdraw from nation bank."));
                return false;
            }
        } else {
            player.sendMessage(configManager.getMessage("nations.withdraw-failed",
                    "§cFailed to withdraw from nation bank."));
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Withdraw money from your nation's bank";
    }

    @Override
    public String getUsage() {
        return "/nation withdraw <amount>";
    }

    @Override
    public String getPermission() {
        return "empiresofalan.nation.withdraw";
    }
}