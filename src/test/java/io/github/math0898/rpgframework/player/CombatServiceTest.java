package io.github.math0898.rpgframework.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CombatServiceTest {
    @Test
    void combatExpiresAtConfiguredTimeout() {
        AtomicLong ticker = new AtomicLong();
        CombatService combat = new CombatService(Duration.ofSeconds(10L), ticker::get);
        UUID playerId = UUID.randomUUID();

        assertFalse(combat.inCombat(playerId));

        combat.markCombat(playerId);
        assertTrue(combat.inCombat(playerId));

        ticker.addAndGet(Duration.ofSeconds(10L).toNanos() - 1L);
        assertTrue(combat.inCombat(playerId));

        ticker.incrementAndGet();
        assertFalse(combat.inCombat(playerId));
    }
}
