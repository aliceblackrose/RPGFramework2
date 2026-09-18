package io.github.math0898.rpgframework.damage.events;

import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityEvent;

public final class VerifiedDeathEvent extends EntityEvent {
    private static final HandlerList HANDLERS = new HandlerList();

    private final AdvancedDamageEvent damageEvent;

    public VerifiedDeathEvent(AdvancedDamageEvent damageEvent) {
        super(damageEvent.getEntity());
        this.damageEvent = damageEvent;
    }

    public AdvancedDamageEvent getDamageEvent() {
        return damageEvent;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
