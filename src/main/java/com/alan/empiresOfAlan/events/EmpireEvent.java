package com.alan.empiresOfAlan.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base class for all EmpiresOfAlan events
 */
public abstract class EmpireEvent extends Event {
    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancelled = false;

    public EmpireEvent() {
        super();
    }

    public EmpireEvent(boolean async) {
        super(async);
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

    /**
     * Check if this event is cancelled
     * Note: Not all events are cancellable
     *
     * @return true if cancelled
     */
    public boolean isCancelled() {
        return this.cancelled;
    }

    /**
     * Set whether this event is cancelled
     * Note: Not all events are cancellable
     *
     * @param cancelled true to cancel
     */
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}