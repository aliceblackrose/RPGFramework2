package io.github.math0898.rpgframework;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class CooldownTest {
    @Test
    void usesMonotonicTickerAndExpiresExactly() {
        AtomicLong ticker = new AtomicLong(1_000L);
        Cooldown cooldown = new Cooldown(ticker::get);

        assertTrue(cooldown.ready());

        cooldown.start(Duration.ofNanos(50L));
        assertFalse(cooldown.ready());
        assertEquals(Duration.ofNanos(50L), cooldown.remaining());

        ticker.addAndGet(49L);
        assertEquals(Duration.ofNanos(1L), cooldown.remaining());

        ticker.incrementAndGet();
        assertTrue(cooldown.ready());
        assertEquals(Duration.ZERO, cooldown.remaining());
    }

    @Test
    void rejectsNegativeDurations() {
        Cooldown cooldown = new Cooldown();
        assertThrows(IllegalArgumentException.class, () -> cooldown.start(Duration.ofSeconds(-1L)));
    }
}
