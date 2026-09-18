package io.github.math0898.rpgframework.classes;

import io.github.math0898.rpgframework.player.PlayerProfile;
import io.github.math0898.rpgframework.player.Talent;
import java.util.Objects;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class ClassService {
    private static final double BASE_HEALTH = 20.0;
    private static final double BASE_SPEED = 0.1;

    public void apply(Player player, PlayerProfile profile) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(profile, "profile");

        Classes type = profile.combatClass();
        double maxHealth = BASE_HEALTH + (type.healthBonus() / 5.0)
                + profile.talentPoints(Talent.HEALTH);
        double movementSpeed = BASE_SPEED * (1.0 + type.speedBonus()
                + (profile.talentPoints(Talent.SPEED) * 0.03));

        setBase(player, Attribute.MAX_HEALTH, Math.max(1.0, maxHealth));
        setBase(player, Attribute.MOVEMENT_SPEED, Math.max(0.01, movementSpeed));

        if (player.getHealth() > maxHealth) {
            player.setHealth(maxHealth);
        }
    }

    private static void setBase(Player player, Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
