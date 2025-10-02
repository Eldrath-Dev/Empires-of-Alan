package com.alan.empiresOfAlan.events.nation;

import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.enums.NationRole;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a resident is promoted in a nation
 */
public class NationPromoteEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Nation nation;
    private final Resident resident;
    private final Player promoter;
    private final NationRole oldRole;
    private final NationRole newRole;
    private boolean cancelled;

    public NationPromoteEvent(Nation nation, Resident resident, Player promoter, NationRole oldRole, NationRole newRole) {
        this.nation = nation;
        this.resident = resident;
        this.promoter = promoter;
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
     * Get the resident being promoted
     *
     * @return The resident
     */
    public Resident getResident() {
        return resident;
    }

    /**
     * Get the player doing the promotion
     *
     * @return The promoter, or null if promoted by console/system
     */
    public Player getPromoter() {
        return promoter;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}