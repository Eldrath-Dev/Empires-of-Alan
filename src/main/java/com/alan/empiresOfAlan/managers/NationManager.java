package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;

public class NationManager {
    private static NationManager instance;
    private final Map<UUID, Nation> nations;
    private final Map<String, UUID> nationNameToId;

    private NationManager() {
        this.nations = new HashMap<>();
        this.nationNameToId = new HashMap<>();
    }

    public static NationManager getInstance() {
        if (instance == null) {
            instance = new NationManager();
        }
        return instance;
    }

    /**
     * Get a nation by UUID
     *
     * @param nationId Nation UUID
     * @return Nation object or null if not found
     */
    public Nation getNation(UUID nationId) {
        return nations.get(nationId);
    }

    /**
     * Get a nation by name
     *
     * @param nationName Nation name
     * @return Nation object or null if not found
     */
    public Nation getNation(String nationName) {
        UUID nationId = nationNameToId.get(nationName.toLowerCase());
        return nationId != null ? nations.get(nationId) : null;
    }

    /**
     * Check if a nation name exists
     *
     * @param nationName Nation name to check
     * @return true if the name is already taken
     */
    public boolean nationExists(String nationName) {
        return nationNameToId.containsKey(nationName.toLowerCase());
    }

    /**
     * Create a new nation
     *
     * @param name Nation name
     * @param capitalTownId Town UUID of the capital
     * @param founderId Resident UUID of the founder
     * @return The new nation, or null if creation failed
     */
    public Nation createNation(String name, UUID capitalTownId, UUID founderId) {
        // Check if name is already taken
        if (nationExists(name)) {
            return null;
        }

        TownManager townManager = TownManager.getInstance();
        Town capitalTown = townManager.getTown(capitalTownId);

        if (capitalTown == null) {
            return null;
        }

        // Check if town is already in a nation
        if (capitalTown.hasNation()) {
            return null;
        }

        // Check if founder is the town owner
        if (!capitalTown.getOwnerId().equals(founderId)) {
            return null;
        }

        // Create the nation
        UUID nationId = UUID.randomUUID();
        Nation nation = new Nation(nationId, name, capitalTownId, founderId);

        // Update the capital town
        capitalTown.setNationId(nationId);

        // Update all residents of the capital town
        ResidentManager residentManager = ResidentManager.getInstance();
        for (UUID residentId : capitalTown.getResidents()) {
            Resident resident = residentManager.getResident(residentId);
            if (resident != null) {
                resident.setNationId(nationId);

                // Set the founder as King, everyone else as Member
                if (residentId.equals(founderId)) {
                    resident.setNationRole(NationRole.KING);
                } else {
                    resident.setNationRole(NationRole.MEMBER);
                }
            }
        }

        // Register the nation
        nations.put(nationId, nation);
        nationNameToId.put(name.toLowerCase(), nationId);

        return nation;
    }

    /**
     * Create a new nation with event support
     *
     * @param name Nation name
     * @param capitalTownId Town UUID of the capital
     * @param founderId Resident UUID of the founder
     * @param founder Founding player
     * @return The new nation, or null if creation failed
     */
    public Nation createNation(String name, UUID capitalTownId, UUID founderId, Player founder) {
        // Check if name is already taken
        if (nationExists(name)) {
            return null;
        }

        TownManager townManager = TownManager.getInstance();
        Town capitalTown = townManager.getTown(capitalTownId);

        if (capitalTown == null) {
            return null;
        }

        // Check if town is already in a nation
        if (capitalTown.hasNation()) {
            return null;
        }

        // Check if founder is the town owner
        if (!capitalTown.getOwnerId().equals(founderId)) {
            return null;
        }

        // Create the nation
        UUID nationId = UUID.randomUUID();
        Nation nation = new Nation(nationId, name, capitalTownId, founderId);

        // Update the capital town
        capitalTown.setNationId(nationId);

        // Update all residents of the capital town
        ResidentManager residentManager = ResidentManager.getInstance();
        for (UUID residentId : capitalTown.getResidents()) {
            Resident resident = residentManager.getResident(residentId);
            if (resident != null) {
                resident.setNationId(nationId);

                // Set the founder as King, everyone else as Member
                if (residentId.equals(founderId)) {
                    resident.setNationRole(NationRole.KING);
                } else {
                    resident.setNationRole(NationRole.MEMBER);
                }
            }
        }

        // Register the nation
        nations.put(nationId, nation);
        nationNameToId.put(name.toLowerCase(), nationId);

        return nation;
    }

    /**
     * Delete a nation
     *
     * @param nationId Nation UUID
     * @return true if successful, false if nation not found
     */
    public boolean deleteNation(UUID nationId) {
        Nation nation = getNation(nationId);
        if (nation == null) {
            return false;
        }

        TownManager townManager = TownManager.getInstance();
        ResidentManager residentManager = ResidentManager.getInstance();

        // Remove all towns from the nation
        for (UUID townId : new ArrayList<>(nation.getTowns())) {
            Town town = townManager.getTown(townId);
            if (town != null) {
                town.setNationId(null);

                // Remove all residents of this town from the nation
                for (UUID residentId : town.getResidents()) {
                    Resident resident = residentManager.getResident(residentId);
                    if (resident != null) {
                        resident.leaveNation();
                    }
                }
            }
        }

        // Remove the nation
        nationNameToId.remove(nation.getName().toLowerCase());
        nations.remove(nationId);

        return true;
    }

    /**
     * Delete a nation with event support
     *
     * @param nationId Nation UUID
     * @param deleter Player deleting the nation
     * @return true if successful, false if nation not found
     */
    public boolean deleteNation(UUID nationId, Player deleter) {
        Nation nation = getNation(nationId);
        if (nation == null) {
            return false;
        }

        TownManager townManager = TownManager.getInstance();
        ResidentManager residentManager = ResidentManager.getInstance();

        // Remove all towns from the nation
        for (UUID townId : new ArrayList<>(nation.getTowns())) {
            Town town = townManager.getTown(townId);
            if (town != null) {
                town.setNationId(null);

                // Remove all residents of this town from the nation
                for (UUID residentId : town.getResidents()) {
                    Resident resident = residentManager.getResident(residentId);
                    if (resident != null) {
                        resident.leaveNation();
                    }
                }
            }
        }

        // Remove the nation
        nationNameToId.remove(nation.getName().toLowerCase());
        nations.remove(nationId);

        return true;
    }

    /**
     * Add a town to a nation
     *
     * @param nationId Nation UUID
     * @param townId Town UUID
     * @return true if successful, false otherwise
     */
    public boolean addTown(UUID nationId, UUID townId) {
        Nation nation = getNation(nationId);
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townId);

        if (nation == null || town == null) {
            return false;
        }

        // Check if town is already in a nation
        if (town.hasNation()) {
            return false;
        }

        // Add town to nation
        nation.addTown(townId);
        town.setNationId(nationId);

        // Add all town residents to the nation
        ResidentManager residentManager = ResidentManager.getInstance();
        for (UUID residentId : town.getResidents()) {
            Resident resident = residentManager.getResident(residentId);
            if (resident != null) {
                resident.setNationId(nationId);
                resident.setNationRole(NationRole.MEMBER);
            }
        }

        return true;
    }

    /**
     * Remove a town from a nation
     *
     * @param nationId Nation UUID
     * @param townId Town UUID
     * @return true if successful, false otherwise
     */
    public boolean removeTown(UUID nationId, UUID townId) {
        Nation nation = getNation(nationId);
        TownManager townManager = TownManager.getInstance();
        Town town = townManager.getTown(townId);

        if (nation == null || town == null) {
            return false;
        }

        // Check if town is in this nation
        if (!town.hasNation() || !town.getNationId().equals(nationId)) {
            return false;
        }

        // Cannot remove the capital
        if (nation.getCapitalId().equals(townId)) {
            return false;
        }

        // Remove town from nation
        nation.removeTown(townId);
        town.setNationId(null);

        // Remove all town residents from the nation
        ResidentManager residentManager = ResidentManager.getInstance();
        for (UUID residentId : town.getResidents()) {
            Resident resident = residentManager.getResident(residentId);
            if (resident != null) {
                resident.leaveNation();
            }
        }

        return true;
    }

    /**
     * Set the nation spawn point
     *
     * @param nationId Nation UUID
     * @param location Spawn location
     * @return true if successful, false otherwise
     */
    public boolean setSpawn(UUID nationId, Location location) {
        Nation nation = getNation(nationId);
        if (nation == null) {
            return false;
        }

        // Check if location is in a town that belongs to this nation
        ClaimManager claimManager = ClaimManager.getInstance();
        TownManager townManager = TownManager.getInstance();
        UUID claimTownId = claimManager.getTownAt(location.getChunk());

        if (claimTownId == null) {
            return false;
        }

        Town town = townManager.getTown(claimTownId);
        if (town == null || !town.hasNation() || !town.getNationId().equals(nationId)) {
            return false;
        }

        nation.setSpawn(location);
        return true;
    }

    /**
     * Promote a resident in a nation
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

        // Check if both are in the same nation
        if (!promoter.hasNation() || !target.hasNation() ||
                !promoter.getNationId().equals(target.getNationId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!promoter.getNationRole().isAtLeast(NationRole.OFFICER) ||
                promoter.getNationRole().getLevel() <= target.getNationRole().getLevel()) {
            return false;
        }

        // Cannot promote to king
        if (target.getNationRole() == NationRole.OFFICER) {
            return false;
        }

        return residentManager.promoteNationRole(targetId);
    }

    /**
     * Promote a resident in a nation with event support
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

        // Check if both are in the same nation
        if (!promoterResident.hasNation() || !target.hasNation() ||
                !promoterResident.getNationId().equals(target.getNationId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!promoterResident.getNationRole().isAtLeast(NationRole.OFFICER) ||
                promoterResident.getNationRole().getLevel() <= target.getNationRole().getLevel()) {
            return false;
        }

        // Cannot promote to king
        if (target.getNationRole() == NationRole.OFFICER) {
            return false;
        }

        return residentManager.promoteNationRole(targetId);
    }

    /**
     * Demote a resident in a nation
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

        // Check if both are in the same nation
        if (!demoter.hasNation() || !target.hasNation() ||
                !demoter.getNationId().equals(target.getNationId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!demoter.getNationRole().isAtLeast(NationRole.OFFICER) ||
                demoter.getNationRole().getLevel() <= target.getNationRole().getLevel()) {
            return false;
        }

        // Check if target is already at minimum role
        if (target.getNationRole() == NationRole.MEMBER) {
            return false;
        }

        return residentManager.demoteNationRole(targetId);
    }

    /**
     * Demote a resident in a nation with event support
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

        // Check if both are in the same nation
        if (!demoterResident.hasNation() || !target.hasNation() ||
                !demoterResident.getNationId().equals(target.getNationId())) {
            return false;
        }

        // Check permission - must be at least one level higher
        if (!demoterResident.getNationRole().isAtLeast(NationRole.OFFICER) ||
                demoterResident.getNationRole().getLevel() <= target.getNationRole().getLevel()) {
            return false;
        }

        // Check if target is already at minimum role
        if (target.getNationRole() == NationRole.MEMBER) {
            return false;
        }

        return residentManager.demoteNationRole(targetId);
    }

    /**
     * Transfer leadership of a nation
     *
     * @param currentLeaderId Current leader UUID
     * @param newLeaderId New leader UUID
     * @return true if successful, false otherwise
     */
    public boolean transferLeadership(UUID currentLeaderId, UUID newLeaderId) {
        ResidentManager residentManager = ResidentManager.getInstance();
        Resident currentLeader = residentManager.getResident(currentLeaderId);
        Resident newLeader = residentManager.getResident(newLeaderId);

        if (currentLeader == null || newLeader == null) {
            return false;
        }

        // Check if both are in the same nation
        if (!currentLeader.hasNation() || !newLeader.hasNation() ||
                !currentLeader.getNationId().equals(newLeader.getNationId())) {
            return false;
        }

        // Check if current leader is actually the king
        Nation nation = getNation(currentLeader.getNationId());
        if (nation == null || !nation.getLeaderId().equals(currentLeaderId)) {
            return false;
        }

        // Transfer leadership
        nation.setLeaderId(newLeaderId);
        currentLeader.setNationRole(NationRole.OFFICER); // Demote current leader to Officer
        newLeader.setNationRole(NationRole.KING);        // Promote new leader to King

        return true;
    }

    /**
     * Change the capital of a nation
     *
     * @param nationId Nation UUID
     * @param newCapitalId New capital town UUID
     * @return true if successful, false otherwise
     */
    public boolean changeCapital(UUID nationId, UUID newCapitalId) {
        Nation nation = getNation(nationId);
        TownManager townManager = TownManager.getInstance();
        Town newCapital = townManager.getTown(newCapitalId);

        if (nation == null || newCapital == null) {
            return false;
        }

        // Check if town is in this nation
        if (!newCapital.hasNation() || !newCapital.getNationId().equals(nationId)) {
            return false;
        }

        return nation.changeCapital(newCapitalId);
    }

    /**
     * Deposit money into a nation's bank
     *
     * @param nationId Nation UUID
     * @param amount Amount to deposit
     * @return true if successful, false otherwise
     */
    public boolean depositToBank(UUID nationId, double amount) {
        Nation nation = getNation(nationId);
        if (nation == null || amount <= 0) {
            return false;
        }

        return nation.getBankAccount().deposit(amount);
    }

    /**
     * Withdraw money from a nation's bank
     *
     * @param nationId Nation UUID
     * @param amount Amount to withdraw
     * @return true if successful, false otherwise
     */
    public boolean withdrawFromBank(UUID nationId, double amount) {
        Nation nation = getNation(nationId);
        if (nation == null || amount <= 0) {
            return false;
        }

        return nation.getBankAccount().withdraw(amount);
    }

    /**
     * Set the tax rate for a nation
     *
     * @param nationId Nation UUID
     * @param taxRate New tax rate (0-100)
     * @return true if successful, false otherwise
     */
    public boolean setTaxRate(UUID nationId, double taxRate) {
        Nation nation = getNation(nationId);
        if (nation == null) {
            return false;
        }

        nation.setTaxRate(taxRate);
        return true;
    }

    /**
     * Get all nations
     *
     * @return Map of all nations
     */
    public Map<UUID, Nation> getAllNations() {
        return new HashMap<>(nations);
    }

    /**
     * Get the internal nations map (for database access)
     *
     * @return The nations map
     */
    public Map<UUID, Nation> getNations() {
        return nations;
    }

    /**
     * Get the internal nation name to ID map (for database access)
     *
     * @return The nation name to ID map
     */
    public Map<String, UUID> getNationNameToId() {
        return nationNameToId;
    }
}