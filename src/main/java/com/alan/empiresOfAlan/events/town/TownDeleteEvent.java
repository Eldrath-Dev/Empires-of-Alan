package com.alan.empiresOfAlan.events.town;

import com.alan.empiresOfAlan.events.EmpireEvent;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import java.util.UUID;

/**
 * Event that is called when a town is deleted
 */
public class TownDeleteEvent extends EmpireEvent implements Cancellable {
    private final Town town;
    private final Player deleter;
    private boolean cancelled;

    public TownDeleteEvent(Town town, Player deleter) {
        super(false); // Sync event
        this.town = town;
        this.deleter = deleter;
        this.cancelled = false;
    }

    /**
     * Get the town being deleted
     *
     * @return The town
     */
    public Town getTown() {
        return town;
    }

    /**
     * Get the player deleting the town
     *
     * @return The deleter, or null if deleted by console/system
     */
    public Player getDeleter() {
        return deleter;
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