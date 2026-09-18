package io.github.math0898.rpgframework.damage.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public final class LethalDamageEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final AdvancedDamageEvent damageEvent;
    private boolean cancelled;

    public LethalDamageEvent(AdvancedDamageEvent damageEvent) {
        super(damageEvent.getEntity());
        this.damageEvent = damageEvent;
    }

    public AdvancedDamageEvent getDamageEvent() {
        return damageEvent;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        cancelled = cancel;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
