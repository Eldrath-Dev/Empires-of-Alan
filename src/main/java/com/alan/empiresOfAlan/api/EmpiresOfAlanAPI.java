package com.alan.empiresOfAlan.api;

import com.alan.empiresOfAlan.EmpiresOfAlan;
import com.alan.empiresOfAlan.events.claim.ClaimAddedEvent;
import com.alan.empiresOfAlan.events.claim.ClaimRemovedEvent;
import com.alan.empiresOfAlan.events.nation.*;
import com.alan.empiresOfAlan.events.town.*;
import com.alan.empiresOfAlan.managers.*;
import com.alan.empiresOfAlan.model.*;
import com.alan.empiresOfAlan.model.bank.BankAccount;
import com.alan.empiresOfAlan.model.enums.ClaimFlag;
import com.alan.empiresOfAlan.model.enums.NationRole;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

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

    // Manager Accessors
    public ResidentManager getResidentManager() {
        return ResidentManager.getInstance();
    }

    public TownManager getTownManager() {
        return TownManager.getInstance();
    }

    public NationManager getNationManager() {
        return NationManager.getInstance();
    }

    public ClaimManager getClaimManager() {
        return ClaimManager.getInstance();
    }

    public TaxManager getTaxManager() {
        return TaxManager.getInstance();
    }

    // Resident Methods
    public Resident getResident(UUID uuid) {
        return getResidentManager().getResident(uuid);
    }

    public Resident getResident(Player player) {
        return getResidentManager().getResident(player.getUniqueId());
    }

    public Resident getResident(String playerName) {
        return getResidentManager().getResident(playerName);
    }

    // Town Methods
    public Town getTown(UUID uuid) {
        return getTownManager().getTown(uuid);
    }

    public Town getTown(String name) {
        return getTownManager().getTown(name);
    }

    public boolean townExists(String name) {
        return getTownManager().townExists(name);
    }

    public Map<UUID, Town> getAllTowns() {
        return getTownManager().getAllTowns();
    }

    public List<Town> getTownsByNation(UUID nationId) {
        return getAllTowns().values().stream()
                .filter(town -> town.hasNation() && town.getNationId().equals(nationId))
                .collect(Collectors.toList());
    }

    // Nation Methods
    public Nation getNation(UUID uuid) {
        return getNationManager().getNation(uuid);
    }

    public Nation getNation(String name) {
        return getNationManager().getNation(name);
    }

    public boolean nationExists(String name) {
        return getNationManager().nationExists(name);
    }

    public Map<UUID, Nation> getAllNations() {
        return getNationManager().getAllNations();
    }

    // Claim Methods
    public Claim getClaim(UUID uuid) {
        return getClaimManager().getClaim(uuid);
    }

    public Claim getClaim(Chunk chunk) {
        return getClaimManager().getClaimAt(chunk);
    }

    public boolean isClaimed(Chunk chunk) {
        return getClaimManager().isClaimed(chunk);
    }

    public UUID getTownAt(Chunk chunk) {
        return getClaimManager().getTownAt(chunk);
    }

    public Map<UUID, Claim> getAllClaims() {
        return getClaimManager().getAllClaims();
    }

    // Permission Methods
    public boolean canBuild(Chunk chunk, Player player) {
        return getClaimManager().canBuild(chunk, player.getUniqueId());
    }

    public boolean canInteract(Chunk chunk, Player player) {
        return getClaimManager().canInteract(chunk, player.getUniqueId());
    }

    public boolean isPvPAllowed(Chunk chunk) {
        return getClaimManager().isPvPAllowed(chunk);
    }

    public boolean areExplosionsAllowed(Chunk chunk) {
        return getClaimManager().areExplosionsAllowed(chunk);
    }

    public boolean isMobSpawningAllowed(Chunk chunk) {
        return getClaimManager().isMobSpawningAllowed(chunk);
    }

    public boolean isFireSpreadAllowed(Chunk chunk) {
        return getClaimManager().isFireSpreadAllowed(chunk);
    }

    // Economy Methods
    public boolean depositToTownBank(UUID townId, double amount) {
        return getTownManager().depositToBank(townId, amount);
    }

    public boolean withdrawFromTownBank(UUID townId, double amount) {
        return getTownManager().withdrawFromBank(townId, amount);
    }

    public boolean depositToNationBank(UUID nationId, double amount) {
        return getNationManager().depositToBank(nationId, amount);
    }

    public boolean withdrawFromNationBank(UUID nationId, double amount) {
        return getNationManager().withdrawFromBank(nationId, amount);
    }

    public double getTownBalance(UUID townId) {
        Town town = getTown(townId);
        return town != null ? town.getBankAccount().getBalance() : 0.0;
    }

    public double getNationBalance(UUID nationId) {
        Nation nation = getNation(nationId);
        return nation != null ? nation.getBankAccount().getBalance() : 0.0;
    }

    // Event-Based Operations (These will fire events)
    public Town createTown(String name, Player founder) {
        // Check if town already exists
        if (townExists(name)) {
            return null;
        }

        ResidentManager residentManager = getResidentManager();
        Resident resident = residentManager.getOrCreateResident(founder);

        // Check if player is already in a town
        if (resident.hasTown()) {
            return null;
        }

        // Fire event
        Town town = new Town(UUID.randomUUID(), name, resident.getUuid());
        TownCreateEvent event = new TownCreateEvent(town, founder);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        // Proceed with creation
        if (getTownManager().createTown(name, founder) != null) {
            return town;
        }

        return null;
    }

    public boolean deleteTown(UUID townId, Player deleter) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        // Fire event
        TownDeleteEvent event = new TownDeleteEvent(town, deleter);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with deletion
        return getTownManager().deleteTown(townId);
    }

    public Nation createNation(String name, UUID capitalTownId, UUID founderId, Player founder) {
        // Check if nation already exists
        if (nationExists(name)) {
            return null;
        }

        Town capitalTown = getTown(capitalTownId);
        if (capitalTown == null) {
            return null;
        }

        // Fire event
        Nation nation = new Nation(UUID.randomUUID(), name, capitalTownId, founderId);
        Town capital = getTown(capitalTownId);
        NationCreateEvent event = new NationCreateEvent(nation, capital, founder);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        // Proceed with creation
        if (getNationManager().createNation(name, capitalTownId, founderId) != null) {
            return nation;
        }

        return null;
    }

    public boolean deleteNation(UUID nationId, Player deleter) {
        Nation nation = getNation(nationId);
        if (nation == null) {
            return false;
        }

        // Fire event
        NationDeleteEvent event = new NationDeleteEvent(nation, deleter);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with deletion
        return getNationManager().deleteNation(nationId);
    }

    public Claim claimChunk(Chunk chunk, UUID townId, Player player) {
        Town town = getTown(townId);
        if (town == null) {
            return null;
        }

        // Check if already claimed
        if (isClaimed(chunk)) {
            return null;
        }

        // Fire event
        Claim claim = new Claim(UUID.randomUUID(), chunk.getWorld().getName(), chunk.getX(), chunk.getZ(), townId);
        ClaimAddedEvent event = new ClaimAddedEvent(claim, town, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return null;
        }

        // Proceed with claiming
        Claim result = getClaimManager().claimChunk(chunk, townId, player);
        if (result != null) {
            // Add claim to town
            getTownManager().addClaim(townId, result.getId());
        }

        return result;
    }

    public boolean unclaimChunk(Chunk chunk, Player player) {
        Claim claim = getClaim(chunk);
        if (claim == null) {
            return false;
        }

        Town town = getTown(claim.getTownId());
        if (town == null) {
            return false;
        }

        // Fire event
        ClaimRemovedEvent event = new ClaimRemovedEvent(claim, town, player);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with unclaiming
        return getClaimManager().unclaimChunk(chunk, player);
    }

    public boolean promoteTownResident(UUID promoterId, UUID targetId, Player promoter) {
        Resident target = getResident(targetId);
        if (target == null || !target.hasTown()) {
            return false;
        }

        Town town = getTown(target.getTownId());
        if (town == null) {
            return false;
        }

        TownRole oldRole = target.getTownRole();
        TownRole newRole = TownRole.getByLevel(oldRole.getLevel() + 1);

        // Fire event
        TownPromoteEvent event = new TownPromoteEvent(town, target, promoter, oldRole, newRole);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with promotion
        return getResidentManager().promoteTownRole(targetId);
    }

    public boolean demoteTownResident(UUID demoterId, UUID targetId, Player demoter) {
        Resident target = getResident(targetId);
        if (target == null || !target.hasTown()) {
            return false;
        }

        Town town = getTown(target.getTownId());
        if (town == null) {
            return false;
        }

        TownRole oldRole = target.getTownRole();
        TownRole newRole = TownRole.getByLevel(oldRole.getLevel() - 1);

        // Fire event
        TownDemoteEvent event = new TownDemoteEvent(town, target, demoter, oldRole, newRole);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with demotion
        return getResidentManager().demoteTownRole(targetId);
    }

    public boolean promoteNationResident(UUID promoterId, UUID targetId, Player promoter) {
        Resident target = getResident(targetId);
        if (target == null || !target.hasNation()) {
            return false;
        }

        Nation nation = getNation(target.getNationId());
        if (nation == null) {
            return false;
        }

        NationRole oldRole = target.getNationRole();
        NationRole newRole = NationRole.getByLevel(oldRole.getLevel() + 1);

        // Fire event
        NationPromoteEvent event = new NationPromoteEvent(nation, target, promoter, oldRole, newRole);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with promotion
        return getResidentManager().promoteNationRole(targetId);
    }

    public boolean demoteNationResident(UUID demoterId, UUID targetId, Player demoter) {
        Resident target = getResident(targetId);
        if (target == null || !target.hasNation()) {
            return false;
        }

        Nation nation = getNation(target.getNationId());
        if (nation == null) {
            return false;
        }

        NationRole oldRole = target.getNationRole();
        NationRole newRole = NationRole.getByLevel(oldRole.getLevel() - 1);

        // Fire event
        NationDemoteEvent event = new NationDemoteEvent(nation, target, demoter, oldRole, newRole);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            return false;
        }

        // Proceed with demotion
        return getResidentManager().demoteNationRole(targetId);
    }

    // Utility Methods
    public boolean setTownSpawn(UUID townId, Location location) {
        return getTownManager().setSpawn(townId, location);
    }

    public boolean setNationSpawn(UUID nationId, Location location) {
        return getNationManager().setSpawn(nationId, location);
    }

    public boolean setClaimFlag(UUID claimId, ClaimFlag flag, boolean value) {
        return getClaimManager().setFlag(claimId, flag, value);
    }

    public boolean isPlayerInTown(UUID playerId) {
        Resident resident = getResident(playerId);
        return resident != null && resident.hasTown();
    }

    public boolean isPlayerInNation(UUID playerId) {
        Resident resident = getResident(playerId);
        return resident != null && resident.hasNation();
    }

    public UUID getPlayerTownId(UUID playerId) {
        Resident resident = getResident(playerId);
        return resident != null && resident.hasTown() ? resident.getTownId() : null;
    }

    public UUID getPlayerNationId(UUID playerId) {
        Resident resident = getResident(playerId);
        return resident != null && resident.hasNation() ? resident.getNationId() : null;
    }
}