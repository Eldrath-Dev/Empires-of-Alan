package com.alan.empiresOfAlan.events.nation;

import com.alan.empiresOfAlan.model.Nation;
import com.alan.empiresOfAlan.model.Town;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a nation is created
 */
public class NationCreateEvent extends Event implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Nation nation;
    private final Town capital;
    private final Player creator;
    private boolean cancelled;

    public NationCreateEvent(Nation nation, Town capital, Player creator) {
        this.nation = nation;
        this.capital = capital;
        this.creator = creator;
        this.cancelled = false;
    }

    /**
     * Get the nation being created
     *
     * @return The nation
     */
    public Nation getNation() {
        return nation;
    }

    /**
     * Get the capital town of the nation
     *
     * @return The capital town
     */
    public Town getCapital() {
        return capital;
    }

    /**
     * Get the player creating the nation
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