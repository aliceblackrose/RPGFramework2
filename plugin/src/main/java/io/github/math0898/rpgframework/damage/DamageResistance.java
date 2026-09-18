package io.github.math0898.rpgframework.damage;

public enum DamageResistance {
    IMMUNITY(-2, 0.0),
    RESISTANCE(-1, 0.5),
    NORMAL(0, 1.0),
    SUSCEPTIBILITY(1, 1.5),
    VULNERABILITY(2, 2.0);

    private final int level;
    private final double multiplier;

    DamageResistance(int level, double multiplier) {
        this.level = level;
        this.multiplier = multiplier;
    }

    public double apply(double damage) {
        return Math.max(0.0, damage) * multiplier;
    }

    public static DamageResistance mergeResistances(DamageResistance first, DamageResistance second) {
        return getResistance(first.level + second.level);
    }

    public static int getInt(DamageResistance resistance) {
        return resistance.level;
    }

    public static DamageResistance getResistance(int value) {
        if (value <= -2) {
            return IMMUNITY;
        }
        if (value >= 2) {
            return VULNERABILITY;
        }
        return switch (value) {
            case -1 -> RESISTANCE;
            case 0 -> NORMAL;
            case 1 -> SUSCEPTIBILITY;
            default -> throw new IllegalStateException("Unreachable resistance value: " + value);
        };
    }
}
