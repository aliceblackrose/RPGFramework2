package io.github.math0898.rpgframework.player;

import java.util.Objects;

public final class StatCalculator {
    public static final double RPG_HEALTH_PER_VANILLA_HEALTH = 5.0;
    public static final double BASE_RPG_HEALTH = 100.0;
    public static final double BASE_MOVEMENT_SPEED = 0.1;
    public static final double HEALTH_PER_POINT = 5.0;
    public static final double DAMAGE_PER_POINT = 1.0;
    public static final double SPEED_PER_POINT = 0.03;
    public static final double CRIT_CHANCE_PER_POINT = 0.025;

    private StatCalculator() {
    }

    public static PlayerStats calculate(PlayerProfile profile) {
        Objects.requireNonNull(profile, "profile");

        var combatClass = profile.combatClass();
        double rpgHealth = BASE_RPG_HEALTH
                + combatClass.healthBonus()
                + (profile.talentPoints(Talent.HEALTH) * HEALTH_PER_POINT);
        double maxHealth = rpgHealth / RPG_HEALTH_PER_VANILLA_HEALTH;

        double movementSpeed = BASE_MOVEMENT_SPEED
                * (1.0 + combatClass.speedBonus()
                + (profile.talentPoints(Talent.SPEED) * SPEED_PER_POINT));

        double flatDamage = profile.talentPoints(Talent.DAMAGE) * DAMAGE_PER_POINT;
        double criticalChance = Math.min(
                1.0,
                profile.talentPoints(Talent.CRIT_CHANCE) * CRIT_CHANCE_PER_POINT);

        return new PlayerStats(
                Math.max(1.0, maxHealth),
                Math.max(0.01, movementSpeed),
                combatClass.damageMultiplier(),
                Math.max(0.0, flatDamage),
                criticalChance);
    }
}
