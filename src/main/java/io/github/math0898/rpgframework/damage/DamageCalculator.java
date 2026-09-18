package io.github.math0898.rpgframework.damage;

import java.util.Map;
import java.util.Objects;

public final class DamageCalculator {
    private DamageCalculator() {
    }

    public static double calculate(
            Map<DamageType, Double> damages,
            Map<DamageType, DamageResistance> resistances,
            double physicalResistance,
            double magicResistance
    ) {
        Objects.requireNonNull(damages, "damages");
        Objects.requireNonNull(resistances, "resistances");

        double physical = clampUnit(physicalResistance);
        double magic = clampUnit(magicResistance);
        double total = 0.0;

        for (DamageType type : DamageType.values()) {
            double raw = Math.max(0.0, damages.getOrDefault(type, 0.0));
            if (raw == 0.0) {
                continue;
            }
            double archetypeReduced = raw * (1.0 - (type.isPhysical() ? physical : magic));
            DamageResistance resistance = resistances.getOrDefault(type, DamageResistance.NORMAL);
            total += resistance.apply(archetypeReduced);
        }

        return Math.max(0.0, total);
    }

    private static double clampUnit(double value) {
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException("resistance must be finite");
        }
        return Math.max(0.0, Math.min(1.0, value));
    }
}
