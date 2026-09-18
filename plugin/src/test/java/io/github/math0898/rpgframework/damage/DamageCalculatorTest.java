package io.github.math0898.rpgframework.damage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.EnumMap;
import org.junit.jupiter.api.Test;

class DamageCalculatorTest {
    @Test
    void appliesArchetypeAndTypeResistanceInOrder() {
        var damage = new EnumMap<DamageType, Double>(DamageType.class);
        var resistance = new EnumMap<DamageType, DamageResistance>(DamageType.class);
        damage.put(DamageType.SLASH, 30.0);
        damage.put(DamageType.ELECTRIC, 60.0);
        resistance.put(DamageType.SLASH, DamageResistance.VULNERABILITY);
        resistance.put(DamageType.ELECTRIC, DamageResistance.RESISTANCE);

        double result = DamageCalculator.calculate(damage, resistance, 0.50, 1.0 / 3.0);

        assertEquals(50.0, result, 0.0001);
    }

    @Test
    void clampsArchetypeResistanceToValidRange() {
        var damage = new EnumMap<DamageType, Double>(DamageType.class);
        damage.put(DamageType.FIRE, 50.0);

        assertEquals(0.0, DamageCalculator.calculate(damage, new EnumMap<>(DamageType.class), 0.0, 5.0));
    }

    @Test
    void rejectsNonFiniteResistance() {
        assertThrows(IllegalArgumentException.class, () ->
                DamageCalculator.calculate(
                        new EnumMap<>(DamageType.class),
                        new EnumMap<>(DamageType.class),
                        Double.NaN,
                        0.0));
    }
}
