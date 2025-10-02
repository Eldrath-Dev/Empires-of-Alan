package com.alan.empiresOfAlan.events.nation;

import com.alan.empiresOfAlan.model.Nation;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a nation is deleted
 */
public class NationDeleteEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Nation nation;
    private final Player deleter;
    private boolean cancelled;

    public NationDeleteEvent(Nation nation, Player deleter) {
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

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}