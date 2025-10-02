package com.alan.empiresOfAlan.listeners;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class ChatListener implements Listener {
    private final EmpiresOfAlan plugin;
    private final ConfigManager configManager;

    public ChatListener(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null) {
            return;
        }

        // Check if player is in town chat
        if (resident.isInTownChat() && resident.hasTown()) {
            event.setCancelled(true);
            sendTownChat(player, resident, event.getMessage());
            return;
        }

        // Check if player is in nation chat
        if (resident.isInNationChat() && resident.hasNation()) {
            event.setCancelled(true);
            sendNationChat(player, resident, event.getMessage());
            return;
        }

        // For global chat, inject town/nation info into the format
        injectTownNationInfo(event, resident);
    }

    private void injectTownNationInfo(AsyncPlayerChatEvent event, Resident resident) {
        // Check if town/nation display is enabled
        boolean showTown = configManager.getConfig().getBoolean("chat.show-town", true);
        boolean showNation = configManager.getConfig().getBoolean("chat.show-nation", true);

        if (!showTown && !showNation) {
            return;
        }

        TownManager townManager = TownManager.getInstance();
        NationManager nationManager = NationManager.getInstance();

        String townName = "";
        String nationName = "";

        // Get town name
        if (showTown && resident.hasTown()) {
            Town town = townManager.getTown(resident.getTownId());
            if (town != null) {
                townName = town.getName();
            }
        }

        // Get nation name
        if (showNation && resident.hasNation()) {
            Nation nation = nationManager.getNation(resident.getNationId());
            if (nation != null) {
                nationName = nation.getName();
            }
        }

        // Only modify format if we have town/nation info to show
        if (!townName.isEmpty() || !nationName.isEmpty()) {
            String format = configManager.getConfig().getString("chat.format", "[{town}|{nation}] ");

            // Replace placeholders
            format = format.replace("{town}", townName.isEmpty() ? "" : townName);
            format = format.replace("{nation}", nationName.isEmpty() ? "" : nationName);

            // Clean up empty brackets or pipes
            format = format.replace("[]", "");
            format = format.replace("||", "|");
            format = format.replace("[|", "[");
            format = format.replace("|]", "]");

            // Trim and add space if needed
            format = format.trim();
            if (!format.isEmpty() && !format.endsWith(" ")) {
                format += " ";
            }

            // Prepend to existing format
            String currentFormat = event.getFormat();
            event.setFormat(format + currentFormat);
        }
    }

    private void sendTownChat(Player sender, Resident resident, String message) {
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(resident.getTownId());

        if (town == null) {
            return;
        }

        // Format message with town name
        String format = configManager.getMessage("chat.town-format",
                        "&b[{0}] &f{1}&f: &7{2}")
                .replace("{0}", town.getName())
                .replace("{1}", sender.getName())
                .replace("{2}", message);

        // Send to all town members
        for (UUID memberId : town.getResidents()) {
            Player member = sender.getServer().getPlayer(memberId);
            if (member != null && member.isOnline()) {
                member.sendMessage(format);
            }
        }

        // Log to console
        plugin.getLogger().info("[Town: " + town.getName() + "] " + sender.getName() + ": " + message);
    }

    private void sendNationChat(Player sender, Resident resident, String message) {
        NationManager nationManager = NationManager.getInstance();
        Nation nation = nationManager.getNation(resident.getNationId());

        if (nation == null) {
            return;
        }

        // Format message with nation name
        String format = configManager.getMessage("chat.nation-format",
                        "&9[{0}] &f{1}&f: &7{2}")
                .replace("{0}", nation.getName())
                .replace("{1}", sender.getName())
                .replace("{2}", message);

        // Send to all nation members
        TownManager townManager = TownManager.getInstance();
        ResidentManager residentManager = ResidentManager.getInstance();

        for (UUID townId : nation.getTowns()) {
            Town town = townManager.getTown(townId);
            if (town != null) {
                for (UUID memberId : town.getResidents()) {
                    Player member = sender.getServer().getPlayer(memberId);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(format);
                    }
                }
            }
        }

        // Log to console
        plugin.getLogger().info("[Nation: " + nation.getName() + "] " + sender.getName() + ": " + message);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ResidentManager residentManager = ResidentManager.getInstance();

        // Create or update resident
        Resident resident = residentManager.getOrCreateResident(player);
        resident.setLastOnline(System.currentTimeMillis());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        ResidentManager residentManager = ResidentManager.getInstance();

        // Update last online time
        Resident resident = residentManager.getResident(player.getUniqueId());
        if (resident != null) {
            resident.setLastOnline(System.currentTimeMillis());
        }
    }
}