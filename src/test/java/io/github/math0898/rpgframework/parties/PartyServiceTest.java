package io.github.math0898.rpgframework.parties;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class PartyServiceTest {
    @Test
    void inviteExpiresAtConfiguredDeadline() {
        AtomicLong ticker = new AtomicLong();
        PartyService parties = new PartyService(Duration.ofSeconds(60L), ticker::get);
        UUID leader = UUID.randomUUID();
        UUID target = UUID.randomUUID();

        assertTrue(parties.invite(leader, target));
        assertTrue(parties.hasValidInvite(target));

        ticker.addAndGet(Duration.ofSeconds(60L).toNanos());

        assertFalse(parties.hasValidInvite(target));
        assertFalse(parties.accept(target));
    }

    @Test
    void leaderBypassIsCapturedByInvite() {
        PartyService parties = new PartyService();
        UUID leader = UUID.randomUUID();

        for (int index = 0; index < Party.DEFAULT_MAX_SIZE - 1; index++) {
            UUID member = UUID.randomUUID();
            assertTrue(parties.invite(leader, member));
            assertTrue(parties.accept(member));
        }

        UUID regularTarget = UUID.randomUUID();
        assertFalse(parties.invite(leader, regularTarget));

        UUID bypassTarget = UUID.randomUUID();
        assertTrue(parties.invite(leader, bypassTarget, true));
        assertTrue(parties.accept(bypassTarget));
    }
}
