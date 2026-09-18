package io.github.math0898.rpgframework.parties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class PartyTest {
    @Test
    void leadershipTransfersWhenLeaderLeaves() {
        UUID leader = UUID.randomUUID();
        UUID member = UUID.randomUUID();
        Party party = new Party(leader);

        assertTrue(party.add(member, false));
        assertTrue(party.remove(leader));

        assertEquals(member, party.leader());
    }

    @Test
    void defaultCapacityIsSix() {
        Party party = new Party(UUID.randomUUID());
        for (int i = 0; i < 5; i++) {
            assertTrue(party.add(UUID.randomUUID(), false));
        }
        assertFalse(party.add(UUID.randomUUID(), false));
        assertTrue(party.add(UUID.randomUUID(), true));
    }
}
