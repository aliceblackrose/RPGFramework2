package io.github.math0898.rpgframework;

import java.time.Duration;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * A monotonic cooldown backed by a nano-time source.
 */
public final class Cooldown {
    private final LongSupplier ticker;
    private long readyAtNanos;

    public Cooldown() {
        this(System::nanoTime);
    }

    Cooldown(LongSupplier ticker) {
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        this.readyAtNanos = ticker.getAsLong();
    }

    public boolean ready() {
        return remaining().isZero();
    }

    public Duration remaining() {
        long now = ticker.getAsLong();
        long remainingNanos = readyAtNanos - now;
        return remainingNanos <= 0L ? Duration.ZERO : Duration.ofNanos(remainingNanos);
    }

    public void start(Duration duration) {
        Objects.requireNonNull(duration, "duration");
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration must not be negative");
        }

        final long durationNanos;
        try {
            durationNanos = duration.toNanos();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("duration is too large", exception);
        }

        readyAtNanos = ticker.getAsLong() + durationNanos;
    }

    public void reset() {
        readyAtNanos = ticker.getAsLong();
    }
}
