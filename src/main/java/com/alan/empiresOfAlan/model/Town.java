package com.alan.empiresOfAlan.model;

import com.alan.empiresOfAlan.model.bank.BankAccount;
import org.bukkit.Chunk;
import org.bukkit.Location;

import java.util.*;

public class Town {
    private final UUID id;
    private String name;
    private UUID ownerId;
    private final BankAccount bankAccount;
    private final Set<UUID> residents;
    private final Set<UUID> claims;
    private Location spawn;
    private UUID nationId;
    private double taxRate;
    private long lastTaxCollection;
    private boolean isPublic;

    public Town(UUID id, String name, UUID ownerId) {
        this.id = id;
        this.name = name;
        this.ownerId = ownerId;
        this.bankAccount = new BankAccount(id);
        this.residents = new HashSet<>();
        this.claims = new HashSet<>();
        this.residents.add(ownerId); // Owner is a resident
        this.taxRate = 0.0;
        this.lastTaxCollection = System.currentTimeMillis();
        this.isPublic = false;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public Set<UUID> getResidents() {
        return Collections.unmodifiableSet(residents);
    }

    public boolean addResident(UUID residentId) {
        return residents.add(residentId);
    }

    public boolean removeResident(UUID residentId) {
        if (residentId.equals(ownerId)) {
            return false; // Cannot remove the owner
        }
        return residents.remove(residentId);
    }

    public boolean isResident(UUID residentId) {
        return residents.contains(residentId);
    }

    public int getResidentCount() {
        return residents.size();
    }

    public Set<UUID> getClaims() {
        return Collections.unmodifiableSet(claims);
    }

    public boolean addClaim(UUID claimId) {
        return claims.add(claimId);
    }

    public boolean removeClaim(UUID claimId) {
        return claims.remove(claimId);
    }

    public boolean hasClaim(UUID claimId) {
        return claims.contains(claimId);
    }

    public int getClaimCount() {
        return claims.size();
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn(Location spawn) {
        this.spawn = spawn;
    }

    public boolean hasSpawn() {
        return spawn != null;
    }

    public UUID getNationId() {
        return nationId;
    }

    public void setNationId(UUID nationId) {
        this.nationId = nationId;
    }

    public boolean hasNation() {
        return nationId != null;
    }

    public double getTaxRate() {
        return taxRate;
    }

    public void setTaxRate(double taxRate) {
        this.taxRate = Math.max(0, Math.min(taxRate, 100)); // Cap between 0-100%
    }

    public long getLastTaxCollection() {
        return lastTaxCollection;
    }

    public void setLastTaxCollection(long lastTaxCollection) {
        this.lastTaxCollection = lastTaxCollection;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Calculates the maximum claims this town can have
     * Dynamic system based on residents and configuration
     *
     * @return Maximum number of claims allowed
     */
    public int getMaxClaims() {
        // Default values (these should be configurable)
        int baseLimit = 5;
        int perPlayerBonus = 10;
        int fivePlayerBonus = 15;

        int residentCount = getResidentCount();
        int claims = baseLimit + (residentCount * perPlayerBonus);

        // Add bonus for 5+ players
        if (residentCount >= 5) {
            claims += fivePlayerBonus;
        }

        return claims;
    }

    /**
     * Checks if the town can claim more chunks
     *
     * @return true if town is under claim limit, false otherwise
     */
    public boolean canClaimMore() {
        return claims.size() < getMaxClaims();
    }

    /**
     * Transfers ownership of the town to a new resident
     *
     * @param newOwnerId UUID of the new owner
     * @return true if successful, false if the resident is not in the town
     */
    public boolean transferOwnership(UUID newOwnerId) {
        if (!residents.contains(newOwnerId)) {
            return false;
        }

        this.ownerId = newOwnerId;
        return true;
    }
}