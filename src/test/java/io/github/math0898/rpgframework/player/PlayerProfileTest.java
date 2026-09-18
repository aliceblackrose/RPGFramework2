package io.github.math0898.rpgframework.player;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PlayerProfileTest {
    @Test
    void experienceFundsTalentAllocationAndResetRefundsIt() {
        PlayerProfile profile = new PlayerProfile(UUID.randomUUID(), "Alice");
        profile.setExperience(300L);

        assertEquals(3, profile.unallocatedPoints());

        profile.allocateTalent(Talent.HEALTH, 2L);
        profile.allocateTalent(Talent.SPEED, 1L);

        assertEquals(0, profile.unallocatedPoints());
        assertEquals(2L, profile.talentPoints(Talent.HEALTH));
        assertEquals(1L, profile.talentPoints(Talent.SPEED));
        assertThrows(IllegalStateException.class, () -> profile.allocateTalent(Talent.DAMAGE, 1L));

        profile.resetTalents();

        assertEquals(3, profile.unallocatedPoints());
        assertEquals(0L, profile.spentTalentPoints());
    }

    @Test
    void cappedTalentsCannotExceedTheirMaximum() {
        PlayerProfile profile = new PlayerProfile(UUID.randomUUID(), "Alice");
        profile.setExperience(10_000L);

        profile.allocateTalent(Talent.CRIT_CHANCE, Talent.CRIT_CHANCE.maxPoints());

        assertThrows(IllegalStateException.class,
                () -> profile.allocateTalent(Talent.CRIT_CHANCE, 1L));
    }
}
