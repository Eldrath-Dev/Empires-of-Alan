package com.alan.empiresOfAlan.commands.town;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.BaseCommand;
import com.alan.empiresOfAlan.commands.town.subcommands.*;

public class TownCommand extends BaseCommand {

    public TownCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    protected void registerSubCommands() {
        subCommands.put("create", new TownCreateCommand(plugin));
        subCommands.put("delete", new TownDeleteCommand(plugin));
        subCommands.put("claim", new TownClaimCommand(plugin));
        subCommands.put("unclaim", new TownUnclaimCommand(plugin));
        subCommands.put("promote", new TownPromoteCommand(plugin));
        subCommands.put("demote", new TownDemoteCommand(plugin));
        subCommands.put("spawn", new TownSpawnCommand(plugin));
        subCommands.put("setspawn", new TownSetSpawnCommand(plugin));
        subCommands.put("deposit", new TownDepositCommand(plugin));
        subCommands.put("withdraw", new TownWithdrawCommand(plugin));
        subCommands.put("chat", new TownChatCommand(plugin));
        subCommands.put("info", new TownInfoCommand(plugin));
        subCommands.put("invite", new TownInviteCommand(plugin));
        subCommands.put("join", new TownJoinCommand(plugin));
        subCommands.put("leave", new TownLeaveCommand(plugin));
        subCommands.put("kick", new TownKickCommand(plugin));
        subCommands.put("list", new TownListCommand(plugin)); // Added list command
    }

    @Override
    public String getCommandName() {
        return "town";
    }
}