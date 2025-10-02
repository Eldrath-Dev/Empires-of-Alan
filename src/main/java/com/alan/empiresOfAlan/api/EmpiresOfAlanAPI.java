package com.alan.empiresOfAlan.api;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.managers.ClaimManager;
import com.alan.empiresOfAlan.managers.NationManager;
import com.alan.empiresOfAlan.managers.ResidentManager;
import com.alan.empiresOfAlan.managers.TownManager;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.ClaimFlag;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

/**
 * Public API for the EmpiresOfAlan plugin
 */
public class EmpiresOfAlanAPI {
    private static EmpiresOfAlanAPI instance;
    private final EmpiresOfAlan plugin;

    private EmpiresOfAlanAPI(EmpiresOfAlan plugin) {
        this.plugin = plugin;
    }

    /**
     * Get the API instance
     *
     * @param plugin The EmpiresOfAlan plugin instance
     * @return The API instance
     */
    public static EmpiresOfAlanAPI getInstance(EmpiresOfAlan plugin) {
        if (instance == null) {
            instance = new EmpiresOfAlanAPI(plugin);
        }
        return instance;
    }

    /**
     * Get the ResidentManager
     *
     * @return The ResidentManager
     */
    public ResidentManager getResidentManager() {
        return ResidentManager.getInstance();
    }

    /**
     * Get the TownManager
     *
     * @return The TownManager
     */
    public TownManager getTownManager() {
        return TownManager.getInstance();
    }

    /**
     * Get the NationManager
     *
     * @return The NationManager
     */
    public NationManager getNationManager() {
        return NationManager.getInstance();
    }

    /**
     * Get the ClaimManager
     *
     * @return The ClaimManager
     */
    public ClaimManager getClaimManager() {
        return ClaimManager.getInstance();
    }

    /**
     * Get a resident by UUID
     *
     * @param uuid The player UUID
     * @return The resident or null if not found
     */
    public Resident getResident(UUID uuid) {
        return getResidentManager().getResident(uuid);
    }

    /**
     * Get a resident by player
     *
     * @param player The player
     * @return The resident or null if not found
     */
    public Resident getResident(Player player) {
        return getResidentManager().getResident(player.getUniqueId());
    }

    /**
     * Get a town by UUID
     *
     * @param uuid The town UUID
     * @return The town or null if not found
     */
    public Town getTown(UUID uuid) {
        return getTownManager().getTown(uuid);
    }

    /**
     * Get a town by name
     *
     * @param name The town name
     * @return The town or null if not found
     */
    public Town getTown(String name) {
        return getTownManager().getTown(name);
    }

    /**
     * Get a nation by UUID
     *
     * @param uuid The nation UUID
     * @return The nation or null if not found
     */
    public Nation getNation(UUID uuid) {
        return getNationManager().getNation(uuid);
    }

    /**
     * Get a nation by name
     *
     * @param name The nation name
     * @return The nation or null if not found
     */
    public Nation getNation(String name) {
        return getNationManager().getNation(name);
    }

    /**
     * Get a claim at a specific chunk
     *
     * @param chunk The chunk
     * @return The claim or null if not claimed
     */
    public Claim getClaim(Chunk chunk) {
        return getClaimManager().getClaimAt(chunk);
    }

    /**
     * Check if a chunk is claimed
     *
     * @param chunk The chunk
     * @return true if claimed, false otherwise
     */
    public boolean isClaimed(Chunk chunk) {
        return getClaimManager().isClaimed(chunk);
    }

    /**
     * Get the town that owns a claim at a specific chunk
     *
     * @param chunk The chunk
     * @return Town UUID or null if not claimed
     */
    public UUID getTownAt(Chunk chunk) {
        return getClaimManager().getTownAt(chunk);
    }

    /**
     * Check if a player can build in a chunk
     *
     * @param chunk The chunk
     * @param player The player
     * @return true if allowed, false otherwise
     */
    public boolean canBuild(Chunk chunk, Player player) {
        return getClaimManager().canBuild(chunk, player.getUniqueId());
    }

    /**
     * Check if a player can interact with blocks in a chunk
     *
     * @param chunk The chunk
     * @param player The player
     * @return true if allowed, false otherwise
     */
    public boolean canInteract(Chunk chunk, Player player) {
        return getClaimManager().canInteract(chunk, player.getUniqueId());
    }

    /**
     * Check if PvP is allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if PvP allowed, false otherwise
     */
    public boolean isPvPAllowed(Chunk chunk) {
        return getClaimManager().isPvPAllowed(chunk);
    }

    /**
     * Check if explosions are allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if explosions allowed, false otherwise
     */
    public boolean areExplosionsAllowed(Chunk chunk) {
        return getClaimManager().areExplosionsAllowed(chunk);
    }

    /**
     * Check if mob spawning is allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if mob spawning allowed, false otherwise
     */
    public boolean isMobSpawningAllowed(Chunk chunk) {
        return getClaimManager().isMobSpawningAllowed(chunk);
    }

    /**
     * Check if fire spread is allowed in a chunk
     *
     * @param chunk The chunk
     * @return true if fire spread allowed, false otherwise
     */
    public boolean isFireSpreadAllowed(Chunk chunk) {
        return getClaimManager().isFireSpreadAllowed(chunk);
    }

    /**
     * Create a town
     *
     * @param name Town name
     * @param founder Founding player
     * @return The new town, or null if creation failed
     */
    public Town createTown(String name, Player founder) {
        return getTownManager().createTown(name, founder);
    }

    /**
     * Delete a town
     *
     * @param townId Town UUID
     * @param deleter Player deleting the town
     * @return true if successful, false otherwise
     */
    public boolean deleteTown(UUID townId, Player deleter) {
        return getTownManager().deleteTown(townId, deleter);
    }

    /**
     * Create a nation
     *
     * @param name Nation name
     * @param capitalTownId Capital town UUID
     * @param founderId Founder player UUID
     * @param founder Founding player
     * @return The new nation, or null if creation failed
     */
    public Nation createNation(String name, UUID capitalTownId, UUID founderId, Player founder) {
        return getNationManager().createNation(name, capitalTownId, founderId, founder);
    }

    /**
     * Delete a nation
     *
     * @param nationId Nation UUID
     * @param deleter Player deleting the nation
     * @return true if successful, false otherwise
     */
    public boolean deleteNation(UUID nationId, Player deleter) {
        return getNationManager().deleteNation(nationId, deleter);
    }

    /**
     * Claim a chunk for a town
     *
     * @param chunk Chunk to claim
     * @param townId Town UUID
     * @param player Player making the claim
     * @return The new claim, or null if failed
     */
    public Claim claimChunk(Chunk chunk, UUID townId, Player player) {
        return getClaimManager().claimChunk(chunk, townId, player);
    }

    /**
     * Unclaim a chunk
     *
     * @param chunk Chunk to unclaim
     * @param player Player unclaiming
     * @return true if successful, false otherwise
     */
    public boolean unclaimChunk(Chunk chunk, Player player) {
        return getClaimManager().unclaimChunk(chunk, player);
    }

    /**
     * Set a claim flag
     *
     * @param claimId Claim UUID
     * @param flag Flag to set
     * @param value Flag value
     * @return true if successful, false otherwise
     */
    public boolean setClaimFlag(UUID claimId, ClaimFlag flag, boolean value) {
        return getClaimManager().setFlag(claimId, flag, value);
    }

    /**
     * Promote a resident in a town
     *
     * @param promoterId Promoter UUID
     * @param targetId Target UUID
     * @param promoter Promoter player
     * @return true if successful, false otherwise
     */
    public boolean promoteTownResident(UUID promoterId, UUID targetId, Player promoter) {
        return getTownManager().promoteResident(promoterId, targetId, promoter);
    }

    /**
     * Demote a resident in a town
     *
     * @param demoterId Demoter UUID
     * @param targetId Target UUID
     * @param demoter Demoter player
     * @return true if successful, false otherwise
     */
    public boolean demoteTownResident(UUID demoterId, UUID targetId, Player demoter) {
        return getTownManager().demoteResident(demoterId, targetId, demoter);
    }

    /**
     * Promote a resident in a nation
     *
     * @param promoterId Promoter UUID
     * @param targetId Target UUID
     * @param promoter Promoter player
     * @return true if successful, false otherwise
     */
    public boolean promoteNationResident(UUID promoterId, UUID targetId, Player promoter) {
        return getNationManager().promoteResident(promoterId, targetId, promoter);
    }

    /**
     * Demote a resident in a nation
     *
     * @param demoterId Demoter UUID
     * @param targetId Target UUID
     * @param demoter Demoter player
     * @return true if successful, false otherwise
     */
    public boolean demoteNationResident(UUID demoterId, UUID targetId, Player demoter) {
        return getNationManager().demoteResident(demoterId, targetId, demoter);
    }
}