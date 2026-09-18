package io.github.math0898.rpgframework.player;

import java.util.Locale;

public enum Talent {
    HEALTH("health", "Health", Long.MAX_VALUE, "+5 RPG health per point"),
    DAMAGE("damage", "Damage", Long.MAX_VALUE, "+1 RPG damage per point"),
    SPEED("speed", "Speed", 10, "+3% movement speed per point"),
    CRIT_CHANCE("crit_chance", "Critical Chance", 40, "+2.5% critical chance per point");

    private final String storageKey;
    private final String displayName;
    private final long maxPoints;
    private final String description;

    Talent(String storageKey, String displayName, long maxPoints, String description) {
        this.storageKey = storageKey;
        this.displayName = displayName;
        this.maxPoints = maxPoints;
        this.description = description;
    }

    public String storageKey() {
        return storageKey;
    }

    public String displayName() {
        return displayName;
    }

    public long maxPoints() {
        return maxPoints;
    }

    public String description() {
        return description;
    }

    public static Talent fromInput(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("talent must not be blank");
        }
        return valueOf(input.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
