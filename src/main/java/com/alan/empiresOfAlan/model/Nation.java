package com.alan.empiresOfAlan.model;

import com.alan.empiresOfAlan.model.bank.BankAccount;
import org.bukkit.Location;

import java.util.*;

public class Nation {
    private final UUID id;
    private String name;
    private UUID capitalId; // Town ID of the capital
    private UUID leaderId; // Resident ID of the leader (King)
    private final BankAccount bankAccount;
    private final Set<UUID> towns;
    private Location spawn;
    private double taxRate;
    private long lastTaxCollection;
    private boolean isPublic;

    public Nation(UUID id, String name, UUID capitalId, UUID leaderId) {
        this.id = id;
        this.name = name;
        this.capitalId = capitalId;
        this.leaderId = leaderId;
        this.bankAccount = new BankAccount(id);
        this.towns = new HashSet<>();
        this.towns.add(capitalId); // Capital is a member town
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

    public UUID getCapitalId() {
        return capitalId;
    }

    public void setCapitalId(UUID capitalId) {
        if (towns.contains(capitalId)) {
            this.capitalId = capitalId;
        }
    }

    public UUID getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(UUID leaderId) {
        this.leaderId = leaderId;
    }

    public BankAccount getBankAccount() {
        return bankAccount;
    }

    public Set<UUID> getTowns() {
        return Collections.unmodifiableSet(towns);
    }

    public boolean addTown(UUID townId) {
        return towns.add(townId);
    }

    public boolean removeTown(UUID townId) {
        if (townId.equals(capitalId)) {
            return false; // Cannot remove the capital
        }
        return towns.remove(townId);
    }

    public boolean hasTown(UUID townId) {
        return towns.contains(townId);
    }

    public int getTownCount() {
        return towns.size();
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
     * Transfers leadership of the nation to a new resident
     *
     * @param newLeaderId UUID of the new leader
     * @return true if successful
     */
    public boolean transferLeadership(UUID newLeaderId) {
        this.leaderId = newLeaderId;
        return true;
    }

    /**
     * Changes the capital town of the nation
     *
     * @param newCapitalId UUID of the new capital town
     * @return true if successful, false if town is not in the nation
     */
    public boolean changeCapital(UUID newCapitalId) {
        if (!towns.contains(newCapitalId)) {
            return false;
        }

        this.capitalId = newCapitalId;
        return true;
    }
}