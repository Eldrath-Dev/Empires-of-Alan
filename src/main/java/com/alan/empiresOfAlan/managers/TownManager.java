package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class TownManager {
    private static TownManager instance;
    private final Map<UUID, Town> towns;
    private final Map<String, UUID> townNameToId;

    private TownManager() {
        this.towns = new HashMap<>();
        this.townNameToId = new HashMap<>();
    }

    public static TownManager getInstance() {
        if (instance == null) {
            instance = new TownManager();
        }
        return instance;
    }

    /**
     * Get a town by UUID
     *
     * @param townId Town UUID
     * @return Town object or null if not found
     */
    public Town getTown(UUID townId) {
        return towns.get(townId);
    }

    /**
     * Get a town by name
     *
     * @param townName Town name
     * @return Town object or null if not found
     */
    public Town getTown(String townName) {
        UUID townId = townNameToId.get(townName.toLowerCase());
        return townId != null ? towns.get(townId) : null;
    }

    /**
     * Check if a town name exists
     *
     * @param townName Town name to check
     * @return true if the name is already taken
     */
    public boolean townExists(String townName) {
        return townNameToId.containsKey(townName.toLowerCase());
    }

    /**
     * Create a new town
     *
     * @param name Town name
     * @param founder Founding player
     * @return The new town, or null if creation failed
     */
    public Town createTown(String name, Player founder) {
        // Check if name is already taken
        if (townExists(name)) {
            return null;
        }

        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getOrCreateResident(founder);

        // Check if player is already in a town
        if (resident.hasTown()) {
            return null;
        }

        // Create the town
        UUID townId = UUID.randomUUID();
        Town town = new Town(townId, name, resident.getUuid());

        // Add the founder as owner
        resident.setTownId(townId);
        resident.setTownRole(TownRole.OWNER);

        // Register the town
        towns.put(townId, town);
        townNameToId.put(name.toLowerCase(), townId);

        return town;
    }

    /**
     * Delete a town
     *
     * @param townId Town UUID
     * @return true if successful, false if town not found
     */
    public boolean deleteTown(UUID townId) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        // Remove town from nation if it belongs to one
        if (town.hasNation()) {
            NationManager nationManager = NationManager.getInstance();
            Nation nation = nationManager.getNation(town.getNationId());
            if (nation != null) {
                if (nation.getCapitalId().equals(townId)) {
                    // If town is the nation's capital, delete the nation
                    nationManager.deleteNation(nation.getId());
                } else {
                    // Otherwise just remove the town from the nation
                    nation.removeTown(townId);
                }
            }
        }

        // Remove all residents from the town
        ResidentManager residentManager = ResidentManager.getInstance();
        for (UUID residentId : new ArrayList<>(town.getResidents())) {
            Resident resident = residentManager.getResident(residentId);
            if (resident != null) {
                resident.leaveTown();
            }
        }

        // Remove all claims
        ClaimManager claimManager = ClaimManager.getInstance();
        for (UUID claimId : new ArrayList<>(town.getClaims())) {
            claimManager.unclaimChunk(claimId);
        }

        // Remove the town
        townNameToId.remove(town.getName().toLowerCase());
        towns.remove(townId);

        return true;
    }

    /**
     * Delete a town with event support
     *
     * @param townId Town UUID
     * @param deleter Player deleting the town
     * @return true if successful, false if town not found
     */
    public boolean deleteTown(UUID townId, Player deleter) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        // Remove town from nation if it belongs to one
        if (town.hasNation()) {
            NationManager nationManager = NationManager.getInstance();
            Nation nation = nationManager.getNation(town.getNationId());
            if (nation != null) {
                if (nation.getCapitalId().equals(townId)) {
                    // If town is the nation's capital, delete the nation
                    nationManager.deleteNation(nation.getId());
                } else {
                    // Otherwise just remove the town from the nation
                    nation.removeTown(townId);
                }
            }
        }

        // Remove all residents from the town
        ResidentManager residentManager = ResidentManager.getInstance();
        for (UUID residentId : new ArrayList<>(town.getResidents())) {
            Resident resident = residentManager.getResident(residentId);
            if (resident != null) {
                resident.leaveTown();
            }
        }

        // Remove all claims
        ClaimManager claimManager = ClaimManager.getInstance();
        for (UUID claimId : new ArrayList<>(town.getClaims())) {
            claimManager.unclaimChunk(claimId);
        }

        // Remove the town
        townNameToId.remove(town.getName().toLowerCase());
        towns.remove(townId);

        return true;
    }

    /**
     * Add a resident to a town
     *
     * @param townId Town UUID
     * @param residentId Resident UUID
     * @return true if successful, false otherwise
     */
    public boolean addResident(UUID townId, UUID residentId) {
        Town town = getTown(townId);
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(residentId);

        if (town == null || resident == null) {
            return false;
        }

        // Check if resident is already in a town
        if (resident.hasTown()) {
            return false;
        }

        // Add resident to town
        town.addResident(residentId);
        resident.setTownId(townId);
        resident.setTownRole(TownRole.MEMBER);

        return true;
    }

    /**
     * Remove a resident from a town
     *
     * @param townId Town UUID
     * @param residentId Resident UUID
     * @return true if successful, false otherwise
     */
    public boolean removeResident(UUID townId, UUID residentId) {
        Town town = getTown(townId);
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident resident = residentManager.getResident(residentId);

        if (town == null || resident == null) {
            return false;
        }

        // Check if resident is in this town
        if (!resident.hasTown() || !resident.getTownId().equals(townId)) {
            return false;
        }

        // Cannot remove the owner
        if (town.getOwnerId().equals(residentId)) {
            return false;
        }

        // Remove resident from town
        town.removeResident(residentId);
        resident.leaveTown();

        return true;
    }

    /**
     * Set the town spawn point
     *
     * @param townId Town UUID
     * @param location Spawn location
     * @return true if successful, false otherwise
     */
    public boolean setSpawn(UUID townId, Location location) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        // Check if location is in a town claim
        ClaimManager claimManager = ClaimManager.getInstance();
        if (!claimManager.isTownClaim(location.getChunk(), townId)) {
            return false;
        }

        town.setSpawn(location);
        return true;
    }

    /**
     * Promote a resident in a town
     *
     * @param promoterId The resident doing the promotion
     * @param targetId The resident being promoted
     * @return true if successful, false otherwise
     */
    public boolean promoteResident(UUID promoterId, UUID targetId) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident promoter = residentManager.getResident(promoterId);
        Resident target = residentManager.getResident(targetId);

        if (promoter == null || target == null) {
            return false;
        }

        // Check if both are in the same town
        if (!promoter.hasTown() || !target.hasTown() ||
                !promoter.getTownId().equals(target.getTownId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!promoter.getTownRole().isAtLeast(TownRole.MAYOR) ||
                promoter.getTownRole().getLevel() <= target.getTownRole().getLevel()) {
            return false;
        }

        // Cannot promote to owner
        if (target.getTownRole() == TownRole.MAYOR) {
            return false;
        }

        return residentManager.promoteTownRole(targetId);
    }

    /**
     * Promote a resident in a town with event support
     *
     * @param promoterId The resident doing the promotion
     * @param targetId The resident being promoted
     * @param promoter The player doing the promotion
     * @return true if successful, false otherwise
     */
    public boolean promoteResident(UUID promoterId, UUID targetId, Player promoter) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident promoterResident = residentManager.getResident(promoterId);
        Resident target = residentManager.getResident(targetId);

        if (promoterResident == null || target == null) {
            return false;
        }

        // Check if both are in the same town
        if (!promoterResident.hasTown() || !target.hasTown() ||
                !promoterResident.getTownId().equals(target.getTownId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!promoterResident.getTownRole().isAtLeast(TownRole.MAYOR) ||
                promoterResident.getTownRole().getLevel() <= target.getTownRole().getLevel()) {
            return false;
        }

        // Cannot promote to owner
        if (target.getTownRole() == TownRole.MAYOR) {
            return false;
        }

        return residentManager.promoteTownRole(targetId);
    }

    /**
     * Demote a resident in a town
     *
     * @param demoterId The resident doing the demotion
     * @param targetId The resident being demoted
     * @return true if successful, false otherwise
     */
    public boolean demoteResident(UUID demoterId, UUID targetId) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident demoter = residentManager.getResident(demoterId);
        Resident target = residentManager.getResident(targetId);

        if (demoter == null || target == null) {
            return false;
        }

        // Check if both are in the same town
        if (!demoter.hasTown() || !target.hasTown() ||
                !demoter.getTownId().equals(target.getTownId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!demoter.getTownRole().isAtLeast(TownRole.MAYOR) ||
                demoter.getTownRole().getLevel() <= target.getTownRole().getLevel()) {
            return false;
        }

        // Check if target is already at minimum role
        if (target.getTownRole() == TownRole.MEMBER) {
            return false;
        }

        return residentManager.demoteTownRole(targetId);
    }

    /**
     * Demote a resident in a town with event support
     *
     * @param demoterId The resident doing the demotion
     * @param targetId The resident being demoted
     * @param demoter The player doing the demotion
     * @return true if successful, false otherwise
     */
    public boolean demoteResident(UUID demoterId, UUID targetId, Player demoter) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident demoterResident = residentManager.getResident(demoterId);
        Resident target = residentManager.getResident(targetId);

        if (demoterResident == null || target == null) {
            return false;
        }

        // Check if both are in the same town
        if (!demoterResident.hasTown() || !target.hasTown() ||
                !demoterResident.getTownId().equals(target.getTownId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!demoterResident.getTownRole().isAtLeast(TownRole.MAYOR) ||
                demoterResident.getTownRole().getLevel() <= target.getTownRole().getLevel()) {
            return false;
        }

        // Check if target is already at minimum role
        if (target.getTownRole() == TownRole.MEMBER) {
            return false;
        }

        return residentManager.demoteTownRole(targetId);
    }

    /**
     * Transfer ownership of a town
     *
     * @param currentOwnerId Current owner UUID
     * @param newOwnerId New owner UUID
     * @return true if successful, false otherwise
     */
    public boolean transferOwnership(UUID currentOwnerId, UUID newOwnerId) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident currentOwner = residentManager.getResident(currentOwnerId);
        Resident newOwner = residentManager.getResident(newOwnerId);

        if (currentOwner == null || newOwner == null) {
            return false;
        }

        // Check if both are in the same town
        if (!currentOwner.hasTown() || !newOwner.hasTown() ||
                !currentOwner.getTownId().equals(newOwner.getTownId())) {
            return false;
        }

        // Check if current owner is actually the owner
        Town town = getTown(currentOwner.getTownId());
        if (town == null || !town.getOwnerId().equals(currentOwnerId)) {
            return false;
        }

        // Transfer ownership
        town.setOwnerId(newOwnerId);
        currentOwner.setTownRole(TownRole.MAYOR); // Demote current owner to Mayor
        newOwner.setTownRole(TownRole.OWNER);     // Promote new owner to Owner

        return true;
    }

    /**
     * Deposit money into a town's bank
     *
     * @param townId Town UUID
     * @param amount Amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean depositToBank(UUID townId, double amount) {
        Town town = getTown(townId);
        if (town == null || amount <= 0) {
            return false;
        }

        return town.getBankAccount().deposit(amount);
    }

    /**
     * Withdraw money from a town's bank
     *
     * @param townId Town UUID
     * @param amount Amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdrawFromBank(UUID townId, double amount) {
        Town town = getTown(townId);
        if (town == null || amount <= 0) {
            return false;
        }

        return town.getBankAccount().withdraw(amount);
    }

    /**
     * Set the tax rate for a town
     *
     * @param townId Town UUID
     * @param taxRate New tax rate (0-100)
     * @return true if successful, false otherwise
     */
    public boolean setTaxRate(UUID townId, double taxRate) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        town.setTaxRate(taxRate);
        return true;
    }

    /**
     * Get all towns
     *
     * @return Map of all towns
     */
    public Map<UUID, Town> getAllTowns() {
        return new HashMap<>(towns);
    }

    /**
     * Add a claim to a town
     *
     * @param townId Town UUID
     * @param claimId Claim UUID
     * @return true if successful, false if town not found or already at max claims
     */
    public boolean addClaim(UUID townId, UUID claimId) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        if (!town.canClaimMore()) {
            return false;
        }

        return town.addClaim(claimId);
    }

    /**
     * Remove a claim from a town
     *
     * @param townId Town UUID
     * @param claimId Claim UUID
     * @return true if successful, false if town not found or doesn't have the claim
     */
    public boolean removeClaim(UUID townId, UUID claimId) {
        Town town = getTown(townId);
        if (town == null) {
            return false;
        }

        return town.removeClaim(claimId);
    }

    /**
     * Get the internal towns map (for database access)
     *
     * @return The towns map
     */
    public Map<UUID, Town> getTowns() {
        return towns;
    }

    /**
     * Get the internal town name to ID map (for database access)
     *
     * @return The town name to ID map
     */
    public Map<String, UUID> getTownNameToId() {
        return townNameToId;
    }
}