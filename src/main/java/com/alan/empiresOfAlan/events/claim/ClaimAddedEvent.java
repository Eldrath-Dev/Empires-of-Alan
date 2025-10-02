package com.alan.empiresOfAlan.events.claim;

import com.alan.empiresOfAlan.events.EmpireEvent;
import com.alan.empiresOfAlan.model.Claim;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event that is called when a claim is added to a town
 */
public class ClaimAddedEvent extends EmpireEvent implements Cancellable {
    private final Claim claim;
    private final Town town;
    private final Player claimer;
    private boolean cancelled;

    public ClaimAddedEvent(Claim claim, Town town, Player claimer) {
        super(false); // Sync event
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
}