package io.github.math0898.rpgframework.player;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

public final class CombatService {
    public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(10);

    private final long timeoutNanos;
    private final LongSupplier ticker;
    private final Map<UUID, Long> lastCombat = new ConcurrentHashMap<>();

    public CombatService() {
        this(DEFAULT_TIMEOUT, System::nanoTime);
    }

    CombatService(Duration timeout, LongSupplier ticker) {
        Objects.requireNonNull(timeout, "timeout");
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        if (timeout.isNegative() || timeout.isZero()) {
            throw new IllegalArgumentException("timeout must be positive");
        }
        try {
            timeoutNanos = timeout.toNanos();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("timeout is too large", exception);
        }
    }

    public void markCombat(UUID... playerIds) {
        long now = ticker.getAsLong();
        for (UUID playerId : playerIds) {
            if (playerId != null) {
                lastCombat.put(playerId, now);
            }
        }
    }

    public boolean inCombat(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        Long markedAt = lastCombat.get(playerId);
        if (markedAt == null) {
            return false;
        }

        if (ticker.getAsLong() - markedAt < timeoutNanos) {
            return true;
        }

        lastCombat.remove(playerId, markedAt);
        return false;
    }

    public void clear(UUID playerId) {
        lastCombat.remove(Objects.requireNonNull(playerId, "playerId"));
    }
}
