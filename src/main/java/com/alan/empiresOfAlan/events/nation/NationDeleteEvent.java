package com.alan.empiresOfAlan.events.nation;

import com.alan.empiresOfAlan.events.EmpireEvent;
import com.alan.empiresOfAlan.model.Nation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

/**
 * Event that is called when a nation is deleted
 */
public class NationDeleteEvent extends EmpireEvent implements Cancellable {
    private final Nation nation;
    private final Player deleter;
    private boolean cancelled;

    public NationDeleteEvent(Nation nation, Player deleter) {
        super(false); // Sync event
        this.nation = nation;
        this.deleter = deleter;
        this.cancelled = false;
    }

    /**
     * Get the nation being deleted
     *
     * @return The nation
     */
    public Nation getNation() {
        return nation;
    }

    /**
     * Get the player deleting the nation
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