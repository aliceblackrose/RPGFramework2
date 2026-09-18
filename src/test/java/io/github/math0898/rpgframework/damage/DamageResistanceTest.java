package io.github.math0898.rpgframework.damage;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DamageResistanceTest {
    @Test
    void mergeSaturatesAtBounds() {
        assertEquals(DamageResistance.IMMUNITY,
                DamageResistance.mergeResistances(DamageResistance.IMMUNITY, DamageResistance.RESISTANCE));
        assertEquals(DamageResistance.VULNERABILITY,
                DamageResistance.mergeResistances(DamageResistance.VULNERABILITY, DamageResistance.SUSCEPTIBILITY));
    }

    @Test
    void opposingResistancesCancel() {
        assertEquals(DamageResistance.NORMAL,
                DamageResistance.mergeResistances(DamageResistance.RESISTANCE, DamageResistance.SUSCEPTIBILITY));
    }
}
