package com.alan.empiresOfAlan.events.claim;

import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a claim is added to a town
 */
public class ClaimAddedEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Claim claim;
    private final Town town;
    private final Player claimer;
    private boolean cancelled;

    public ClaimAddedEvent(Claim claim, Town town, Player claimer) {
        this.claim = claim;
        this.town = town;
        this.claimer = claimer;
        this.cancelled = false;
    }

    /**
     * Get the claim being added
     *
     * @return The claim
     */
    public Claim getClaim() {
        return claim;
    }

    /**
     * Get the town the claim is being added to
     *
     * @return The town
     */
    public Town getTown() {
        return town;
    }

    /**
     * Get the player adding the claim
     *
     * @return The claimer, or null if claimed by console/system
     */
    public Player getClaimer() {
        return claimer;
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