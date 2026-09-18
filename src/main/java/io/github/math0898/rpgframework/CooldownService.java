package io.github.math0898.rpgframework;

import java.time.Duration;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class CooldownService {
    private final Map<UUID, Map<String, Cooldown>> cooldowns = new ConcurrentHashMap<>();

    public boolean ready(UUID playerId, String key) {
        Objects.requireNonNull(playerId, "playerId");
        String normalized = normalizeKey(key);
        Map<String, Cooldown> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return true;
        }

        Cooldown cooldown = playerCooldowns.get(normalized);
        return cooldown == null || cooldown.ready();
    }

    public Duration remaining(UUID playerId, String key) {
        Objects.requireNonNull(playerId, "playerId");
        String normalized = normalizeKey(key);
        Map<String, Cooldown> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return Duration.ZERO;
        }

        Cooldown cooldown = playerCooldowns.get(normalized);
        return cooldown == null ? Duration.ZERO : cooldown.remaining();
    }

    public void start(UUID playerId, String key, Duration duration) {
        Objects.requireNonNull(playerId, "playerId");
        Objects.requireNonNull(duration, "duration");
        String normalized = normalizeKey(key);
        cooldowns.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .computeIfAbsent(normalized, ignored -> new Cooldown())
                .start(duration);
    }

    public void reset(UUID playerId, String key) {
        Objects.requireNonNull(playerId, "playerId");
        String normalized = normalizeKey(key);
        Map<String, Cooldown> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) {
            return;
        }
        playerCooldowns.remove(normalized);
        if (playerCooldowns.isEmpty()) {
            cooldowns.remove(playerId, playerCooldowns);
        }
    }

    public void resetAll(UUID playerId) {
        cooldowns.remove(Objects.requireNonNull(playerId, "playerId"));
    }

    public void clear(UUID playerId) {
        resetAll(playerId);
    }

    private static String normalizeKey(String key) {
        Objects.requireNonNull(key, "key");
        String normalized = key.trim().toLowerCase(Locale.ROOT);
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("cooldown key must not be blank");
        }
        return normalized;
    }
}
