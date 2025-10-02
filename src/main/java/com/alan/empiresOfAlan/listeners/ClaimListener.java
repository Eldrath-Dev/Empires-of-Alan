package com.alan.empiresOfAlan.listeners;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.managers.ClaimManager;
import com.alan.empiresOfAlan.util.ConfigManager;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class ClaimListener implements Listener {
    private final EmpiresOfAlan plugin;
    private final ConfigManager configManager;

    public ClaimListener(EmpiresOfAlan plugin) {
        this.plugin = plugin;
        this.configManager = plugin.getConfigManager();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        ClaimManager claimManager = ClaimManager.getInstance();

        // Check if player can build here
        if (!claimManager.canBuild(chunk, player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("claims.cannot-build",
                    "§cYou cannot build in this area."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Chunk chunk = block.getChunk();

        ClaimManager claimManager = ClaimManager.getInstance();

        // Check if player can build here
        if (!claimManager.canBuild(chunk, player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("claims.cannot-build",
                    "§cYou cannot build in this area."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();
        Chunk chunk = block.getChunk();

        // Skip for certain blocks (always allow interactions)
        if (isAlwaysAllowed(block.getType())) {
            return;
        }

        ClaimManager claimManager = ClaimManager.getInstance();

        // Check if player can interact here
        if (!claimManager.canInteract(chunk, player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(configManager.getMessage("claims.cannot-interact",
                    "§cYou cannot interact with blocks in this area."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        Entity target = event.getEntity();

        // Only handle player vs player
        if (!(damager instanceof Player) || !(target instanceof Player)) {
            return;
        }

        Player attacker = (Player) damager;
        Chunk chunk = target.getLocation().getChunk();

        ClaimManager claimManager = ClaimManager.getInstance();

        // Check if PvP is allowed here
        if (!claimManager.isPvPAllowed(chunk)) {
            event.setCancelled(true);
            attacker.sendMessage(configManager.getMessage("claims.pvp-disabled",
                    "§cPvP is disabled in this area."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (event.blockList().isEmpty()) {
            return;
        }

        ClaimManager claimManager = ClaimManager.getInstance();

        // Check each block in the explosion
        event.blockList().removeIf(block -> {
            Chunk chunk = block.getChunk();
            return !claimManager.areExplosionsAllowed(chunk);
        });
    }

    /**
     * Check if a material is always allowed for interaction
     *
     * @param material The material
     * @return true if always allowed
     */
    private boolean isAlwaysAllowed(Material material) {
        return material == Material.CRAFTING_TABLE ||
                material == Material.ENCHANTING_TABLE ||
                material == Material.ENDER_CHEST;
    }
}