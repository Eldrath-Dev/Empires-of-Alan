package com.alan.empiresOfAlan.events.town;

import com.alan.empiresOfAlan.events.EmpireEvent;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event that is called when a resident is demoted in a town
 */
public class TownDemoteEvent extends EmpireEvent implements Cancellable {
    private final Town town;
    private final Resident resident;
    private final Player demoter;
    private final TownRole oldRole;
    private final TownRole newRole;
    private boolean cancelled;

    public TownDemoteEvent(Town town, Resident resident, Player demoter, TownRole oldRole, TownRole newRole) {
        super(false); // Sync event
        this.town = town;
        this.resident = resident;
        this.demoter = demoter;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.cancelled = false;
    }

    /**
     * Get the town
     *
     * @return The town
     */
    public Town getTown() {
        return town;
    }

    /**
     * Get the resident being demoted
     *
     * @return The resident
     */
    public Resident getResident() {
        return resident;
    }

    /**
     * Get the player doing the demotion
     *
     * @return The demoter, or null if demoted by console/system
     */
    public Player getDemoter() {
        return demoter;
    }

    /**
     * Get the old role of the resident
     *
     * @return The old role
     */
    public TownRole getOldRole() {
        return oldRole;
    }

    /**
     * Get the new role of the resident
     *
     * @return The new role
     */
    public TownRole getNewRole() {
        return newRole;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}