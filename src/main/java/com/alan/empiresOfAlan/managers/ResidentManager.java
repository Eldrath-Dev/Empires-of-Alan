package com.alan.empiresOfAlan.managers;

import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.NationRole;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ResidentManager {
    private static ResidentManager instance;
    private final Map<UUID, Resident> residents;

    private ResidentManager() {
        this.residents = new HashMap<>();
    }

    public static ResidentManager getInstance() {
        if (instance == null) {
            instance = new ResidentManager();
        }
        return instance;
    }

    /**
     * Get a resident by UUID
     *
     * @param uuid Player UUID
     * @return Resident object or null if not found
     */
    public Resident getResident(UUID uuid) {
        return residents.get(uuid);
    }

    /**
     * Get a resident by player name
     *
     * @param playerName Player name
     * @return Resident object or null if not found
     */
    public Resident getResident(String playerName) {
        Player player = Bukkit.getPlayer(playerName);
        if (player != null) {
            return getResident(player.getUniqueId());
        }

        // Search by name if no online player found
        for (Resident resident : residents.values()) {
            if (resident.getName().equalsIgnoreCase(playerName)) {
                return resident;
            }
        }

        return null;
    }

    /**
     * Create a new resident for a player
     *
     * @param player The player
     * @return The new resident
     */
    public Resident createResident(Player player) {
        UUID uuid = player.getUniqueId();
        Resident resident = new Resident(uuid, player.getName());
        residents.put(uuid, resident);
        return resident;
    }

    /**
     * Get or create a resident for a player
     *
     * @param player The player
     * @return Existing or new resident
     */
    public Resident getOrCreateResident(Player player) {
        Resident resident = getResident(player.getUniqueId());
        if (resident == null) {
            resident = createResident(player);
        } else {
            // Update name if it has changed
            if (!resident.getName().equals(player.getName())) {
                resident.setName(player.getName());
            }
        }
        return resident;
    }

    /**
     * Update the last online time for a resident
     *
     * @param uuid Player UUID
     */
    public void updateLastOnline(UUID uuid) {
        Resident resident = getResident(uuid);
        if (resident != null) {
            resident.setLastOnline(System.currentTimeMillis());
        }
    }

    /**
     * Add a resident to a town
     *
     * @param residentId Resident UUID
     * @param townId Town UUID
     * @param role Initial role in town
     * @return true if successful, false otherwise
     */
    public boolean addToTown(UUID residentId, UUID townId, TownRole role) {
        Resident resident = getResident(residentId);
        if (resident == null) {
            return false;
        }

        resident.setTownId(townId);
        resident.setTownRole(role);
        return true;
    }

    /**
     * Remove a resident from their town
     *
     * @param residentId Resident UUID
     * @return true if successful, false if resident not found or not in a town
     */
    public boolean removeFromTown(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasTown()) {
            return false;
        }

        resident.leaveTown();
        return true;
    }

    /**
     * Add a resident to a nation
     *
     * @param residentId Resident UUID
     * @param nationId Nation UUID
     * @param role Initial role in nation
     * @return true if successful, false otherwise
     */
    public boolean addToNation(UUID residentId, UUID nationId, NationRole role) {
        Resident resident = getResident(residentId);
        if (resident == null) {
            return false;
        }

        resident.setNationId(nationId);
        resident.setNationRole(role);
        return true;
    }

    /**
     * Remove a resident from their nation
     *
     * @param residentId Resident UUID
     * @return true if successful, false if resident not found or not in a nation
     */
    public boolean removeFromNation(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasNation()) {
            return false;
        }

        resident.leaveNation();
        return true;
    }

    /**
     * Promote a resident in their town
     *
     * @param residentId Resident UUID
     * @return true if successful, false otherwise
     */
    public boolean promoteTownRole(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasTown()) {
            return false;
        }

        TownRole currentRole = resident.getTownRole();
        int nextLevel = currentRole.getLevel() + 1;

        if (nextLevel > TownRole.OWNER.getLevel()) {
            return false; // Already at max level
        }

        resident.setTownRole(TownRole.getByLevel(nextLevel));
        return true;
    }

    /**
     * Demote a resident in their town
     *
     * @param residentId Resident UUID
     * @return true if successful, false otherwise
     */
    public boolean demoteTownRole(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasTown()) {
            return false;
        }

        TownRole currentRole = resident.getTownRole();
        int prevLevel = currentRole.getLevel() - 1;

        if (prevLevel < TownRole.MEMBER.getLevel()) {
            return false; // Already at min level
        }

        resident.setTownRole(TownRole.getByLevel(prevLevel));
        return true;
    }

    /**
     * Promote a resident in their nation
     *
     * @param residentId Resident UUID
     * @return true if successful, false otherwise
     */
    public boolean promoteNationRole(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasNation()) {
            return false;
        }

        NationRole currentRole = resident.getNationRole();
        int nextLevel = currentRole.getLevel() + 1;

        if (nextLevel > NationRole.KING.getLevel()) {
            return false; // Already at max level
        }

        resident.setNationRole(NationRole.getByLevel(nextLevel));
        return true;
    }

    /**
     * Demote a resident in their nation
     *
     * @param residentId Resident UUID
     * @return true if successful, false otherwise
     */
    public boolean demoteNationRole(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasNation()) {
            return false;
        }

        NationRole currentRole = resident.getNationRole();
        int prevLevel = currentRole.getLevel() - 1;

        if (prevLevel < NationRole.MEMBER.getLevel()) {
            return false; // Already at min level
        }

        resident.setNationRole(NationRole.getByLevel(prevLevel));
        return true;
    }

    /**
     * Toggle a resident's town chat status
     *
     * @param residentId Resident UUID
     * @return New chat status, or false if resident not found or not in a town
     */
    public boolean toggleTownChat(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasTown()) {
            return false;
        }

        boolean newStatus = !resident.isInTownChat();
        resident.setTownChat(newStatus);

        // Disable nation chat if town chat is enabled
        if (newStatus && resident.isInNationChat()) {
            resident.setNationChat(false);
        }

        return newStatus;
    }

    /**
     * Toggle a resident's nation chat status
     *
     * @param residentId Resident UUID
     * @return New chat status, or false if resident not found or not in a nation
     */
    public boolean toggleNationChat(UUID residentId) {
        Resident resident = getResident(residentId);
        if (resident == null || !resident.hasNation()) {
            return false;
        }

        boolean newStatus = !resident.isInNationChat();
        resident.setNationChat(newStatus);

        // Disable town chat if nation chat is enabled
        if (newStatus && resident.isInTownChat()) {
            resident.setTownChat(false);
        }

        return newStatus;
    }

    /**
     * Remove a resident from the system (should be called when data is saved to DB)
     *
     * @param uuid Player UUID
     */
    public void removeResident(UUID uuid) {
        residents.remove(uuid);
    }

    /**
     * Get all residents
     *
     * @return Map of all residents
     */
    public Map<UUID, Resident> getAllResidents() {
        return new HashMap<>(residents);
    }

    /**
     * Get the internal residents map (for database access)
     *
     * @return The residents map
     */
    public Map<UUID, Resident> getResidents() {
        return residents;
    }
}