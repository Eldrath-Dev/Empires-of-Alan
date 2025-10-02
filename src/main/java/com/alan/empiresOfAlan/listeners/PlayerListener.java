package com.alan.empiresOfAlan.listeners;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.model.Resident;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final EmpiresOfAlan plugin;

    public PlayerListener(EmpiresOfAlan plugin) {
        this.plugin = plugin;
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