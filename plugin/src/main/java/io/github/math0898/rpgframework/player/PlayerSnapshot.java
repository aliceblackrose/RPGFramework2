package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.classes.Classes;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public record PlayerSnapshot(
        UUID uuid,
        String name,
        Classes combatClass,
        long experience,
        Map<Talent, Long> talentPoints,
        Set<String> artifacts
) {
    public PlayerSnapshot {
        talentPoints = Map.copyOf(talentPoints);
        artifacts = Set.copyOf(artifacts);
    }
}
