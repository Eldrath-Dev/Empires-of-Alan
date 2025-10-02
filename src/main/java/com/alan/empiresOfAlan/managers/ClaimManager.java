package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.events.claim.ClaimAddedEvent;
import com.alan.empiresOfAlan.events.claim.ClaimRemovedEvent;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.ClaimFlag;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClaimManager {
    private static ClaimManager instance;
    private final Map<UUID, Claim> claims;
    private final Map<String, UUID> locationToClaimId; // "world:x:z" -> claim UUID

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
     * Claim a chunk for a town and fire the ClaimAddedEvent
     *
     * @param chunk The chunk to claim
     * @param townId The town claiming it
     * @param player The player making the claim
     * @return The new claim or null if unsuccessful or event was cancelled
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

        // Fire the event
        ClaimAddedEvent event = new ClaimAddedEvent(claim, town, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            // Event was cancelled
            return null;
        }

        // Register the claim
        claims.put(claimId, claim);
        locationToClaimId.put(claim.getLocationKey(), claimId);

        // Add to town
        town.addClaim(claimId);

        return claim;
    }

    /**
     * Unclaim a chunk and fire the ClaimRemovedEvent
     *
     * @param chunk The chunk to unclaim
     * @param player The player doing the unclaiming
     * @return true if successful, false otherwise or if event was cancelled
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

        // Get the town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(claim.getTownId());

        if (town == null) {
            return false;
        }

        // Fire the event
        ClaimRemovedEvent event = new ClaimRemovedEvent(claim, town, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            // Event was cancelled
            return false;
        }

        return unclaimChunk(claim.getId(), player);
    }

    /**
     * Unclaim a chunk by claim ID and fire the ClaimRemovedEvent
     *
     * @param claimId Claim UUID
     * @param player The player doing the unclaiming, or null if unclaimed by console/system
     * @return true if successful, false otherwise
     */
    public boolean unclaimChunk(UUID claimId, Player player) {
        Claim claim = getClaim(claimId);

        if (claim == null) {
            return false;
        }

        // Get the town
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(claim.getTownId());

        // Fire the event (if town exists)
        if (town != null && player != null) {
            ClaimRemovedEvent event = new ClaimRemovedEvent(claim, town, player);
            Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                // Event was cancelled
                return false;
            }

            // Remove from town
            town.removeClaim(claimId);
        }

        // Remove the claim
        locationToClaimId.remove(claim.getLocationKey());
        claims.remove(claimId);

        return true;
    }

    /**
     * Unclaim a chunk by claim ID
     *
     * @param claimId Claim UUID
     * @return true if successful, false otherwise
     */
    public boolean unclaimChunk(UUID claimId) {
        return unclaimChunk(claimId, null);
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
}