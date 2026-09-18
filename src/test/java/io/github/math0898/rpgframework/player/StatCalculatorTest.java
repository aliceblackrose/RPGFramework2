package io.github.math0898.rpgframework.player;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.math0898.rpgframework.classes.Classes;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class StatCalculatorTest {
    @Test
    void combinesClassBonusesAndTalentsUsingRpgScale() {
        PlayerProfile profile = new PlayerProfile(UUID.randomUUID(), "Alice");
        profile.combatClass(Classes.PALADIN);
        profile.setTalentPoints(Talent.HEALTH, 2L);
        profile.setTalentPoints(Talent.SPEED, 1L);
        profile.setTalentPoints(Talent.DAMAGE, 3L);
        profile.setTalentPoints(Talent.CRIT_CHANCE, 4L);

        PlayerStats stats = StatCalculator.calculate(profile);

        assertEquals(29.0, stats.maxHealth(), 0.0001);
        assertEquals(0.101, stats.movementSpeed(), 0.0001);
        assertEquals(0.96, stats.damageMultiplier(), 0.0001);
        assertEquals(3.0, stats.flatDamage(), 0.0001);
        assertEquals(0.10, stats.criticalChance(), 0.0001);
    }
}
