package com.alan.empiresOfAlan.events.town;

import com.alan.empiresOfAlan.model.Resident;
import com.alan.empiresOfAlan.model.Town;
import com.alan.empiresOfAlan.model.enums.TownRole;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a resident is promoted in a town
 */
public class TownPromoteEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Town town;
    private final Resident resident;
    private final Player promoter;
    private final TownRole oldRole;
    private final TownRole newRole;
    private boolean cancelled;

    public TownPromoteEvent(Town town, Resident resident, Player promoter, TownRole oldRole, TownRole newRole) {
        this.town = town;
        this.resident = resident;
        this.promoter = promoter;
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}