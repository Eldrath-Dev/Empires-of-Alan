package com.alan.empiresOfAlan.events.nation;

import com.alan.empiresOfAlan.events.EmpireEvent;
import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event that is called when a resident is demoted in a nation
 */
public class NationDemoteEvent extends EmpireEvent implements Cancellable {
    private final Nation nation;
    private final Resident resident;
    private final Player demoter;
    private final NationRole oldRole;
    private final NationRole newRole;
    private boolean cancelled;

    public NationDemoteEvent(Nation nation, Resident resident, Player demoter, NationRole oldRole, NationRole newRole) {
        super(false); // Sync event
        this.nation = nation;
        this.resident = resident;
        this.demoter = demoter;
        this.oldRole = oldRole;
        this.newRole = newRole;
        this.cancelled = false;
    }

    /**
     * Get the nation
     *
     * @return The nation
     */
    public Nation getNation() {
        return nation;
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
    public NationRole getOldRole() {
        return oldRole;
    }

    /**
     * Get the new role of the resident
     *
     * @return The new role
     */
    public NationRole getNewRole() {
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