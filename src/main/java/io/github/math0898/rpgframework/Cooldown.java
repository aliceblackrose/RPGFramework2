package io.github.math0898.rpgframework;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * Monotonic-by-contract cooldown value based on wall-clock instants.
 */
public final class Cooldown {
    private Instant readyAt = Instant.EPOCH;

    public boolean ready() {
        return !Instant.now().isBefore(readyAt);
    }

    public Duration remaining() {
        var remaining = Duration.between(Instant.now(), readyAt);
        return remaining.isNegative() ? Duration.ZERO : remaining;
    }

    public void start(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration must not be negative");
        }
        readyAt = Instant.now().plus(duration);
    }

    public void reset() {
        readyAt = Instant.EPOCH;
    }
}
