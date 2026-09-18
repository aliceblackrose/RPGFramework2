package io.github.math0898.rpgframework.classes;

import java.util.Locale;

public enum Classes {
    NONE("None", 0.0, 1.0, 0.0),
    ASSASSIN("Assassin", 0.0, 1.10, 0.08),
    BARD("Bard", 10.0, 1.00, 0.04),
    BERSERKER("Berserker", 20.0, 1.12, 0.00),
    PALADIN("Paladin", 35.0, 0.96, -0.02),
    PYROMANCER("Pyromancer", 5.0, 1.08, 0.00);

    private final String displayName;
    private final double healthBonus;
    private final double damageMultiplier;
    private final double speedBonus;

    Classes(String displayName, double healthBonus, double damageMultiplier, double speedBonus) {
        this.displayName = displayName;
        this.healthBonus = healthBonus;
        this.damageMultiplier = damageMultiplier;
        this.speedBonus = speedBonus;
    }

    public String getName() {
        return displayName;
    }

    public double healthBonus() {
        return healthBonus;
    }

    public double damageMultiplier() {
        return damageMultiplier;
    }

    public double speedBonus() {
        return speedBonus;
    }

    public static Classes fromString(String value) {
        if (value == null || value.isBlank()) {
            return NONE;
        }
        try {
            return valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return NONE;
        }
    }
}
