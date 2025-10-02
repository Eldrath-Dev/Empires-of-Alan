package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.ClaimFlag;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimManager {
    private static ClaimManager instance;
    private final Map<UUID, Claim> claims;
    private final Map<String, UUID> locationToClaimId; // "world:x:z" -> claim UUID
    private EmpiresOfAlan plugin;

    private ClaimManager() {
        this.claims = new HashMap<>();
        this.locationToClaimId = new HashMap<>();
    }

    public static ClaimManager getInstance() {
        if (instance == null) {
            instance = new ClaimManager();
        }
        return instance;
    }

    /**
     * Set the plugin reference
     *
     * @param plugin The plugin instance
     */
    public void setPlugin(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    /**
     * Get a claim by UUID
     *
     * @param claimId Claim UUID
     * @return Claim object or null if not found
     */
    public Claim getClaim(UUID claimId) {
        return claims.get(claimId);
    }

    /**
     * Get a claim at a specific chunk
     *
     * @param chunk The chunk
     * @return Claim object or null if not claimed
     */
    public Claim getClaimAt(Chunk chunk) {
        String locationKey = Claim.getLocationKey(chunk);
        UUID claimId = locationToClaimId.get(locationKey);
        return claimId != null ? claims.get(claimId) : null;
    }

    /**
     * Check if a chunk is claimed
     *
     * @param chunk The chunk
     * @return true if claimed, false otherwise
     */
    public boolean isClaimed(Chunk chunk) {
        return locationToClaimId.containsKey(Claim.getLocationKey(chunk));
    }

    /**
     * Get the town that owns a claim at a specific chunk
     *
     * @param chunk The chunk
     * @return Town UUID or null if not claimed
     */
    public UUID getTownAt(Chunk chunk) {
        Claim claim = getClaimAt(chunk);
        return claim != null ? claim.getTownId() : null;
    }

    /**
     * Check if a chunk is claimed by a specific town
     *
     * @param chunk The chunk
     * @param townId Town UUID
     * @return true if claimed by the town, false otherwise
     */
    public boolean isTownClaim(Chunk chunk, UUID townId) {
        UUID claimTownId = getTownAt(chunk);
        return claimTownId != null && claimTownId.equals(townId);
    }

    /**
     * Claim a chunk for a town
     *
     * @param chunk The chunk to claim
     * @param townId The town claiming it
     * @param player The player making the claim
     * @return The new claim or null if unsuccessful
     */
    public Claim claimChunk(Chunk chunk, UUID townId, Player player) {
        // Check if already claimed
        if (isClaimed(chunk)) {
            return null;
        }

        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townId);

        if (town == null) {
            return null;
        }

        // Check if town can claim more chunks
        if (!town.canClaimMore()) {
            return null;
        }

        // Check player permissions
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown() || !resident.getTownId().equals(townId) ||
                !resident.hasTownPermission(TownRole.KNIGHT)) {
            return null;
        }

        // Create the claim
        UUID claimId = UUID.randomUUID();
        Claim claim = new Claim(claimId, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), townId);

        // Register the claim
        claims.put(claimId, claim);
        locationToClaimId.put(claim.getLocationKey(), claimId);

        // Add to town
        town.addClaim(claimId);

        return claim;
    }

    /**
     * Claim a chunk for a town with cost calculation
     *
     * @param chunk The chunk to claim
     * @param townId The town claiming it
     * @param player The player making the claim
     * @return The new claim or null if unsuccessful
     */
    public Claim claimChunkWithCost(Chunk chunk, UUID townId, Player player) {
        // Check if already claimed
        if (isClaimed(chunk)) {
            return null;
        }

        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townId);

        if (town == null) {
            return null;
        }

        // Check if town can claim more chunks
        if (!town.canClaimMore()) {
            return null;
        }

        // Check player permissions
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown() || !resident.getTownId().equals(townId) ||
                !resident.hasTownPermission(TownRole.KNIGHT)) {
            return null;
        }

        // Calculate claim cost
        int freeChunks = plugin.getConfigManager().getConfig().getInt("chunk-claim.free-chunks", 10);
        double claimCost = plugin.getConfigManager().getConfig().getDouble("chunk-claim.claim-cost", 50.0);
        String payPriority = plugin.getConfigManager().getConfig().getString("chunk-claim.pay-priority", "town-first");

        // Check if this is a free chunk
        if (town.getClaims().size() < freeChunks) {
            // Free chunk - no cost
            return claimChunk(chunk, townId, player);
        }

        // This is a paid chunk - check if player/town can afford it
        boolean canAfford = false;

        if ("town-first".equals(payPriority)) {
            // Try town bank first
            if (town.getBankAccount().getBalance() >= claimCost) {
                town.getBankAccount().withdraw(claimCost);
                canAfford = true;
            } else {
                // Try player balance
                if (plugin.getVaultIntegration().has(player, claimCost)) {
                    plugin.getVaultIntegration().withdraw(player, claimCost);
                    canAfford = true;
                }
            }
        } else {
            // Try player balance first
            if (plugin.getVaultIntegration().has(player, claimCost)) {
                plugin.getVaultIntegration().withdraw(player, claimCost);
                canAfford = true;
            } else {
                // Try town bank
                if (town.getBankAccount().getBalance() >= claimCost) {
                    town.getBankAccount().withdraw(claimCost);
                    canAfford = true;
                }
            }
        }

        if (!canAfford) {
            player.sendMessage(plugin.getConfigManager().getMessage("claims.cannot-afford",
                            "&cYou cannot afford to claim this chunk. Cost: &e${0}")
                    .replace("{0}", String.valueOf(claimCost)));
            return null;
        }

        // Create the claim
        UUID claimId = UUID.randomUUID();
        Claim claim = new Claim(claimId, chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), townId);

        // Register the claim
        claims.put(claimId, claim);
        locationToClaimId.put(claim.getLocationKey(), claimId);

        // Add to town
        town.addClaim(claimId);

        // Notify player
        player.sendMessage(plugin.getConfigManager().getMessage("claims.claimed-paid",
                        "&aChunk claimed for town: &e{0} &a(Cost: &e${1}&a)")
                .replace("{0}", town.getName())
                .replace("{1}", String.valueOf(claimCost)));

        return claim;
    }

    /**
     * Unclaim a chunk
     *
     * @param chunk The chunk to unclaim
     * @param player The player doing the unclaiming
     * @return true if successful, false otherwise
     */
    public boolean unclaimChunk(Chunk chunk, Player player) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return false;
        }

        // Check player permissions
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(player.getUniqueId());

        if (resident == null || !resident.hasTown() || !resident.getTownId().equals(claim.getTownId()) ||
                !resident.hasTownPermission(TownRole.KNIGHT)) {
            return false;
        }

        return unclaimChunk(claim.getId());
    }

    /**
     * Unclaim a chunk by claim ID
     *
     * @param claimId Claim UUID
     * @return true if successful, false otherwise
     */
    public boolean unclaimChunk(UUID claimId) {
        Claim claim = getClaim(claimId);

        if (claim == null) {
            return false;
        }

        // Remove from town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(claim.getTownId());

        if (town != null) {
            town.removeClaim(claimId);
        }

        // Remove the claim
        locationToClaimId.remove(claim.getLocationKey());
        claims.remove(claimId);

        return true;
    }

    /**
     * Set a claim flag
     *
     * @param claimId Claim UUID
     * @param flag The flag to set
     * @param value The value to set
     * @return true if successful, false otherwise
     */
    public boolean setFlag(UUID claimId, ClaimFlag flag, boolean value) {
        Claim claim = getClaim(claimId);

        if (claim == null) {
            return false;
        }

        claim.setFlag(flag, value);
        return true;
    }

    /**
     * Check if a player can build in a chunk
     *
     * @param chunk The chunk
     * @param playerId Player UUID
     * @return true if allowed, false otherwise
     */
    public boolean canBuild(Chunk chunk, UUID playerId) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return true; // Unclaimed chunks are buildable by anyone
        }

        // Get player's town and role
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(playerId);

        if (resident == null) {
            return false;
        }

        // Town members can build in their own town
        if (resident.hasTown() && resident.getTownId().equals(claim.getTownId())) {
            return true;
        }

        // Check the BUILD flag for non-members
        return claim.getFlag(ClaimFlag.BUILD);
    }

    /**
     * Check if a player can interact with blocks in a chunk
     *
     * @param chunk The chunk
     * @param playerId Player UUID
     * @return true if allowed, false otherwise
     */
    public boolean canInteract(Chunk chunk, UUID playerId) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return true; // Unclaimed chunks are interactive by anyone
        }

        // Get player's town and role
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(playerId);

        if (resident == null) {
            return false;
        }

        // Town members can interact in their own town
        if (resident.hasTown() && resident.getTownId().equals(claim.getTownId())) {
            return true;
        }

        // Check the INTERACT flag for non-members
        return claim.getFlag(ClaimFlag.INTERACT);
    }

    /**
     * Check if PvP is allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if PvP allowed, false otherwise
     */
    public boolean isPvPAllowed(Chunk chunk) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return true; // Unclaimed chunks allow PvP
        }

        return claim.getFlag(ClaimFlag.PVP);
    }

    /**
     * Check if explosions are allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if explosions allowed, false otherwise
     */
    public boolean areExplosionsAllowed(Chunk chunk) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return true; // Unclaimed chunks allow explosions
        }

        return claim.getFlag(ClaimFlag.EXPLOSIONS);
    }

    /**
     * Check if mob spawning is allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if mob spawning allowed, false otherwise
     */
    public boolean isMobSpawningAllowed(Chunk chunk) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return true; // Unclaimed chunks allow mob spawning
        }

        return claim.getFlag(ClaimFlag.MOB_SPAWNING);
    }

    /**
     * Check if fire spread is allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if fire spread allowed, false otherwise
     */
    public boolean isFireSpreadAllowed(Chunk chunk) {
        Claim claim = getClaimAt(chunk);

        if (claim == null) {
            return true; // Unclaimed chunks allow fire spread
        }

        return claim.getFlag(ClaimFlag.FIRE_SPREAD);
    }

    /**
     * Get all claims
     *
     * @return Map of all claims
     */
    public Map<UUID, Claim> getAllClaims() {
        return new HashMap<>(claims);
    }

    /**
     * Visualize claim borders with particles
     *
     * @param chunk The chunk to visualize
     */
    public void visualizeClaim(Chunk chunk) {
        if (plugin == null) {
            return;
        }

        // Check if visualization is enabled
        if (!plugin.getConfigManager().getConfig().getBoolean("claims.visualization.enabled", true)) {
            return;
        }

        Claim claim = getClaimAt(chunk);
        if (claim == null) {
            return;
        }

        int duration = plugin.getConfigManager().getConfig().getInt("claims.visualization.duration", 10);

        // Schedule particle display
        Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int ticks = 0;
            final int maxTicks = duration * 4; // Convert seconds to ticks (we run every 5 ticks)

            @Override
            public void run() {
                if (ticks >= maxTicks) {
                    // Cancel the task
                    return;
                }

                displayClaimParticles(chunk);
                ticks++;
            }
        }, 0L, 5L); // Run every 5 ticks (0.25 seconds)
    }

    /**
     * Display particles around claim borders
     *
     * @param chunk The chunk to display particles for
     */
    private void displayClaimParticles(Chunk chunk) {
        World world = chunk.getWorld();
        int chunkX = chunk.getX() << 4;
        int chunkZ = chunk.getZ() << 4;
        int minY = world.getMinHeight();
        int maxY = world.getMaxHeight();

        // Get particle type from config
        String particleName = plugin != null ?
                plugin.getConfigManager().getConfig().getString("claims.visualization.particle-type", "FLAME") :
                "FLAME";
        Particle particleType;
        try {
            particleType = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            particleType = Particle.FLAME;
        }

        // Display particles at the four corners
        for (int y = minY; y <= maxY; y += 2) {
            // Corner particles
            world.spawnParticle(particleType, chunkX, y, chunkZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particleType, chunkX + 16, y, chunkZ, 1, 0, 0, 0, 0);
            world.spawnParticle(particleType, chunkX, y, chunkZ + 16, 1, 0, 0, 0, 0);
            world.spawnParticle(particleType, chunkX + 16, y, chunkZ + 16, 1, 0, 0, 0, 0);

            // Border particles (every 4 blocks)
            for (int x = 0; x <= 16; x += 4) {
                world.spawnParticle(particleType, chunkX + x, y, chunkZ, 1, 0, 0, 0, 0);
                world.spawnParticle(particleType, chunkX + x, y, chunkZ + 16, 1, 0, 0, 0, 0);
            }

            for (int z = 0; z <= 16; z += 4) {
                world.spawnParticle(particleType, chunkX, y, chunkZ + z, 1, 0, 0, 0, 0);
                world.spawnParticle(particleType, chunkX + 16, y, chunkZ + z, 1, 0, 0, 0, 0);
            }
        }
    }

    /**
     * Get the internal claims map (for database access)
     *
     * @return The claims map
     */
    public Map<UUID, Claim> getClaims() {
        return claims;
    }

    /**
     * Get the internal location to claim ID map (for database access)
     *
     * @return The location to claim ID map
     */
    public Map<String, UUID> getLocationToClaimId() {
        return locationToClaimId;
    }
}