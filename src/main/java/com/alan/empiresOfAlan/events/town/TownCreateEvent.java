package com.alan.empiresOfAlan.events.town;

import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a town is created
 */
public class TownCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Town town;
    private final Player creator;
    private boolean cancelled;

    public TownCreateEvent(Town town, Player creator) {
        this.town = town;
        this.creator = creator;
        this.cancelled = false;
    }

    /**
     * Get the town being created
     *
     * @return The town
     */
    public Town getTown() {
        return town;
    }

    /**
     * Get the player creating the town
     *
     * @return The creator
     */
    public Player getCreator() {
        return creator;
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