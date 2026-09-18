package io.github.math0898.rpgframework.player;

public record PlayerStats(
        double maxHealth,
        double movementSpeed,
        double damageMultiplier,
        double flatDamage,
        double criticalChance
) {
    public PlayerStats {
        requireFinitePositive(maxHealth, "maxHealth");
        requireFinitePositive(movementSpeed, "movementSpeed");
        requireFinitePositive(damageMultiplier, "damageMultiplier");
        requireFiniteNonNegative(flatDamage, "flatDamage");
        if (!Double.isFinite(criticalChance) || criticalChance < 0.0 || criticalChance > 1.0) {
            throw new IllegalArgumentException("criticalChance must be between 0 and 1");
        }
    }

    private static void requireFinitePositive(double value, String name) {
        if (!Double.isFinite(value) || value <= 0.0) {
            throw new IllegalArgumentException(name + " must be finite and positive");
        }
    }

    private static void requireFiniteNonNegative(double value, String name) {
        if (!Double.isFinite(value) || value < 0.0) {
            throw new IllegalArgumentException(name + " must be finite and non-negative");
        }
    }
}
