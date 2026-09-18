package io.github.math0898.rpgframework.damage.events;

import io.github.math0898.rpgframework.damage.DamageResistance;
import io.github.math0898.rpgframework.damage.DamageType;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityEvent;

public final class AdvancedDamageEvent extends EntityEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();

    private final EntityDamageEvent basicEvent;
    private final EnumMap<DamageType, Double> damages = new EnumMap<>(DamageType.class);
    private final EnumMap<DamageType, DamageResistance> resistances = new EnumMap<>(DamageType.class);
    private double magicResistance;
    private double physicalResistance;
    private boolean critical;
    private boolean cancelled;

    public AdvancedDamageEvent(EntityDamageEvent basicEvent) {
        super(Objects.requireNonNull(basicEvent, "basicEvent").getEntity());
        this.basicEvent = basicEvent;

        for (DamageType type : DamageType.values()) {
            damages.put(type, 0.0);
            resistances.put(type, DamageResistance.NORMAL);
        }
        damages.put(fromCause(basicEvent.getCause()), Math.max(0.0, basicEvent.getDamage() * 5.0));
    }

    public EntityDamageEvent getBasicEvent() {
        return basicEvent;
    }

    public Map<DamageType, Double> getDamages() {
        return Collections.unmodifiableMap(damages);
    }

    public double getDamage(DamageType type) {
        return damages.getOrDefault(Objects.requireNonNull(type, "type"), 0.0);
    }

    public void setDamage(DamageType type, double damage) {
        Objects.requireNonNull(type, "type");
        if (!Double.isFinite(damage)) {
            throw new IllegalArgumentException("damage must be finite");
        }
        damages.put(type, Math.max(0.0, damage));
    }

    public void setDamages(Map<DamageType, Double> replacement) {
        Objects.requireNonNull(replacement, "replacement");
        for (DamageType type : DamageType.values()) {
            setDamage(type, replacement.getOrDefault(type, 0.0));
        }
    }

    public void addDamage(double damage, DamageType type) {
        if (!Double.isFinite(damage)) {
            throw new IllegalArgumentException("damage must be finite");
        }
        DamageType checkedType = Objects.requireNonNull(type, "type");
        damages.merge(checkedType, Math.max(0.0, damage), Double::sum);
    }

    public DamageType getPrimaryDamage() {
        DamageType primary = DamageType.UNSPECIFIED;
        double highest = Double.NEGATIVE_INFINITY;
        for (var entry : damages.entrySet()) {
            if (entry.getValue() > highest) {
                highest = entry.getValue();
                primary = entry.getKey();
            }
        }
        return primary;
    }

    public Map<DamageType, DamageResistance> getResistances() {
        return Collections.unmodifiableMap(resistances);
    }

    public void setResistances(Map<DamageType, DamageResistance> modifiers) {
        Objects.requireNonNull(modifiers, "modifiers");
        for (DamageType type : DamageType.values()) {
            DamageResistance modifier = modifiers.get(type);
            if (modifier != null) {
                resistances.merge(type, modifier, DamageResistance::mergeResistances);
            }
        }
    }

    public void setResistance(DamageType type, DamageResistance resistance) {
        resistances.put(
                Objects.requireNonNull(type, "type"),
                Objects.requireNonNull(resistance, "resistance"));
    }

    public double getMagicResistance() {
        return magicResistance;
    }

    public void setMagicResistance(double resistance) {
        magicResistance = requireFinite(resistance, "magicResistance");
    }

    public double getPhysicalResistance() {
        return physicalResistance;
    }

    public void setPhysicalResistance(double resistance) {
        physicalResistance = requireFinite(resistance, "physicalResistance");
    }

    public boolean isCritical() {
        return critical;
    }

    public void setCritical(boolean critical) {
        this.critical = critical;
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

    private static double requireFinite(double value, String name) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be finite");
        }
        return value;
    }

    private static DamageType fromCause(EntityDamageEvent.DamageCause cause) {
        return switch (cause) {
            case LIGHTNING -> DamageType.ELECTRIC;
            case FIRE, LAVA, FIRE_TICK, HOT_FLOOR -> DamageType.FIRE;
            case FALL, CONTACT, FALLING_BLOCK, FLY_INTO_WALL, ENTITY_EXPLOSION, BLOCK_EXPLOSION -> DamageType.IMPACT;
            case FREEZE -> DamageType.ICE;
            case PROJECTILE -> DamageType.PUNCTURE;
            case THORNS -> DamageType.NATURE;
            case VOID -> DamageType.VOID;
            default -> DamageType.UNSPECIFIED;
        };
    }
}
