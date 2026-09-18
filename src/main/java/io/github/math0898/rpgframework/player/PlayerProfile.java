package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.classes.Classes;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PlayerProfile {
    private final UUID uuid;
    private String name;
    private Classes combatClass;
    private long experience;
    private final EnumMap<Talent, Long> talentPoints = new EnumMap<>(Talent.class);
    private final LinkedHashSet<String> artifacts = new LinkedHashSet<>();

    public PlayerProfile(UUID uuid, String name) {
        this(uuid, name, Classes.NONE, 0L, Map.of(), Set.of());
    }

    public PlayerProfile(
            UUID uuid,
            String name,
            Classes combatClass,
            long experience,
            Map<Talent, Long> talentPoints,
            Set<String> artifacts
    ) {
        this.uuid = Objects.requireNonNull(uuid, "uuid");
        this.name = Objects.requireNonNullElse(name, uuid.toString());
        this.combatClass = Objects.requireNonNullElse(combatClass, Classes.NONE);
        setExperience(experience);
        for (Talent talent : Talent.values()) {
            long points = talentPoints.getOrDefault(talent, 0L);
            this.talentPoints.put(talent, clampPoints(talent, points));
        }
        if (artifacts != null) {
            artifacts.stream().filter(Objects::nonNull).forEach(this.artifacts::add);
        }
    }

    public UUID uuid() {
        return uuid;
    }

    public String name() {
        return name;
    }

    public void updateName(String name) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
    }

    public Classes combatClass() {
        return combatClass;
    }

    public void combatClass(Classes combatClass) {
        this.combatClass = Objects.requireNonNull(combatClass, "combatClass");
    }

    public long experience() {
        return experience;
    }

    public void setExperience(long experience) {
        this.experience = Math.max(0L, experience);
    }

    public void addExperience(long amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        experience = Math.addExact(experience, amount);
    }

    public long talentPoints(Talent talent) {
        return talentPoints.getOrDefault(Objects.requireNonNull(talent), 0L);
    }

    public void setTalentPoints(Talent talent, long points) {
        talentPoints.put(Objects.requireNonNull(talent), clampPoints(talent, points));
    }

    public Set<String> artifacts() {
        return Set.copyOf(artifacts);
    }

    public void addArtifact(String artifact) {
        if (artifact != null && !artifact.isBlank()) {
            artifacts.add(artifact);
        }
    }

    public int unallocatedPoints() {
        long allocated = talentPoints.values().stream().mapToLong(Long::longValue).sum();
        long earned = experience / 100L;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, earned - allocated));
    }

    public PlayerSnapshot snapshot() {
        return new PlayerSnapshot(uuid, name, combatClass, experience, talentPoints, artifacts);
    }

    private static long clampPoints(Talent talent, long points) {
        return Math.max(0L, Math.min(points, talent.maxPoints()));
    }
}
