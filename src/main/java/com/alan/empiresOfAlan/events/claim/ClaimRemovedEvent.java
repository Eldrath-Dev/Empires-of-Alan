package com.alan.empiresOfAlan.events.claim;

import com.alan.empiresOfAlan.events.EmpireEvent;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event that is called when a claim is removed from a town
 */
public class ClaimRemovedEvent extends EmpireEvent implements Cancellable {
    private final Claim claim;
    private final Town town;
    private final Player unclaimer;
    private boolean cancelled;

    public ClaimRemovedEvent(Claim claim, Town town, Player unclaimer) {
        super(false); // Sync event
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
}