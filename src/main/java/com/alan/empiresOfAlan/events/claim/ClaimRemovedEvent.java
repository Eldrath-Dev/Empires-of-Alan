package com.alan.empiresOfAlan.events.claim;

import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a claim is removed from a town
 */
public class ClaimRemovedEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Claim claim;
    private final Town town;
    private final Player unclaimer;
    private boolean cancelled;

    public ClaimRemovedEvent(Claim claim, Town town, Player unclaimer) {
        this.claim = claim;
        this.town = town;
        this.unclaimer = unclaimer;
        this.cancelled = false;
    }

    /**
     * Get the claim being removed
     *
     * @return The claim
     */
    public Claim getClaim() {
        return claim;
    }

    /**
     * Get the town the claim is being removed from
     *
     * @return The town
     */
    public Town getTown() {
        return town;
    }

    /**
     * Get the player removing the claim
     *
     * @return The unclaimer, or null if unclaimed by console/system
     */
    public Player getUnclaimer() {
        return unclaimer;
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