package io.github.math0898.rpgframework.player;

import java.util.Locale;

public enum Talent {
    HEALTH("health", Long.MAX_VALUE),
    DAMAGE("damage", Long.MAX_VALUE),
    SPEED("speed", 10),
    CRIT_CHANCE("crit_chance", 40);

    private final String storageKey;
    private final long maxPoints;

    Talent(String storageKey, long maxPoints) {
        this.storageKey = storageKey;
        this.maxPoints = maxPoints;
    }

    public String storageKey() {
        return storageKey;
    }

    public long maxPoints() {
        return maxPoints;
    }

    public static Talent fromInput(String input) {
        return valueOf(input.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
    }
}
