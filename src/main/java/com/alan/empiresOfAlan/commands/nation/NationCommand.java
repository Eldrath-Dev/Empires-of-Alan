package com.alan.empiresOfAlan.commands.nation;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.commands.BaseCommand;
import com.alan.empiresOfAlan.commands.nation.subcommands.*;

public class NationCommand extends BaseCommand {

    public NationCommand(EmpiresOfAlan plugin) {
        super(plugin);
    }

    @Override
    protected void registerSubCommands() {
        subCommands.put("create", new NationCreateCommand(plugin));
        subCommands.put("delete", new NationDeleteCommand(plugin));
        subCommands.put("promote", new NationPromoteCommand(plugin));
        subCommands.put("demote", new NationDemoteCommand(plugin));
        subCommands.put("spawn", new NationSpawnCommand(plugin));
        subCommands.put("setspawn", new NationSetSpawnCommand(plugin));
        subCommands.put("deposit", new NationDepositCommand(plugin));
        subCommands.put("withdraw", new NationWithdrawCommand(plugin));
        subCommands.put("chat", new NationChatCommand(plugin));
        subCommands.put("info", new NationInfoCommand(plugin));
        subCommands.put("invite", new NationInviteCommand(plugin));
        subCommands.put("join", new NationJoinCommand(plugin));
        subCommands.put("leave", new NationLeaveCommand(plugin));
        subCommands.put("kick", new NationKickCommand(plugin));
    }

    @Override
    public String getCommandName() {
        return "nation";
    }
}