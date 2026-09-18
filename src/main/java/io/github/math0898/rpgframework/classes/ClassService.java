package io.github.math0898.rpgframework.classes;

import io.github.math0898.rpgframework.player.PlayerProfile;
import io.github.math0898.rpgframework.player.PlayerStats;
import io.github.math0898.rpgframework.player.StatCalculator;
import java.util.Objects;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;

public final class ClassService {
    public PlayerStats stats(PlayerProfile profile) {
        return StatCalculator.calculate(profile);
    }

    public void apply(Player player, PlayerProfile profile) {
        Objects.requireNonNull(player, "player");
        PlayerStats stats = stats(Objects.requireNonNull(profile, "profile"));

        setBase(player, Attribute.MAX_HEALTH, stats.maxHealth());
        setBase(player, Attribute.MOVEMENT_SPEED, stats.movementSpeed());

        if (player.getHealth() > stats.maxHealth()) {
            player.setHealth(stats.maxHealth());
        }
    }

    private static void setBase(Player player, Attribute attribute, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }
}
