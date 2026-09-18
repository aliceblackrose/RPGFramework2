package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.classes.Classes;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class PlayerProfile {
    public static final long EXPERIENCE_PER_TALENT_POINT = 100L;

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
        if (amount < 0L) {
            throw new IllegalArgumentException("amount must not be negative");
        }
        experience = Math.addExact(experience, amount);
    }

    public long talentPoints(Talent talent) {
        return talentPoints.getOrDefault(Objects.requireNonNull(talent, "talent"), 0L);
    }

    public void setTalentPoints(Talent talent, long points) {
        talentPoints.put(Objects.requireNonNull(talent, "talent"), clampPoints(talent, points));
    }

    public void allocateTalent(Talent talent, long points) {
        Objects.requireNonNull(talent, "talent");
        if (points <= 0L) {
            throw new IllegalArgumentException("points must be positive");
        }
        if (points > unallocatedPoints()) {
            throw new IllegalStateException("not enough unallocated talent points");
        }

        long current = talentPoints(talent);
        long updated;
        try {
            updated = Math.addExact(current, points);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("talent point total is too large", exception);
        }
        if (updated > talent.maxPoints()) {
            throw new IllegalStateException(talent.displayName() + " is already at its maximum");
        }

        talentPoints.put(talent, updated);
    }

    public void resetTalents() {
        for (Talent talent : Talent.values()) {
            talentPoints.put(talent, 0L);
        }
    }

    public long spentTalentPoints() {
        long spent = 0L;
        for (long points : talentPoints.values()) {
            spent = Math.addExact(spent, points);
        }
        return spent;
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
        long earned = experience / EXPERIENCE_PER_TALENT_POINT;
        long remaining = Math.max(0L, earned - spentTalentPoints());
        return (int) Math.min(Integer.MAX_VALUE, remaining);
    }

    public PlayerSnapshot snapshot() {
        return new PlayerSnapshot(uuid, name, combatClass, experience, talentPoints, artifacts);
    }

    private static long clampPoints(Talent talent, long points) {
        return Math.max(0L, Math.min(points, talent.maxPoints()));
    }
}
