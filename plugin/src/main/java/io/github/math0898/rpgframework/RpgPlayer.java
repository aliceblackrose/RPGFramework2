package io.github.math0898.rpgframework;

import io.github.math0898.rpgframework.classes.Classes;
import io.github.math0898.rpgframework.player.PlayerProfile;
import io.github.math0898.rpgframework.player.Talent;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Deprecated(forRemoval = false)
public final class RpgPlayer {
    public static final double HEALTH_PER_POINT = 5.0;
    public static final double DAMAGE_PER_POINT = 1.0;
    public static final double CRIT_CHANCE_PER_POINT = 0.025;
    public static final long CRIT_CHANCE_MAX_POINTS = 40;
    public static final double MOVEMENT_SPEED_PER_POINT = 0.03;
    public static final long MOVEMENT_SPEED_MAX_POINTS = 10;

    private final UUID uuid;

    public RpgPlayer(Player player) {
        this(player.getUniqueId());
    }

    RpgPlayer(UUID uuid) {
        this.uuid = Objects.requireNonNull(uuid);
    }

    private PlayerProfile profile() {
        return RPGFramework.getInstance().players().find(uuid).orElseThrow(
                () -> new IllegalStateException("Profile is not loaded for " + uuid));
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return profile().name(); }
    public Player getPlayer() { return Bukkit.getPlayer(uuid); }
    public Classes getCombatClass() { return profile().combatClass(); }

    public void joinClass(Classes combatClass) {
        Player player = getPlayer();
        if (player == null) {
            profile().combatClass(combatClass);
            return;
        }
        RPGFramework.getInstance().players().changeClass(player, combatClass);
    }

    public long getExperience() { return profile().experience(); }
    public void setExperience(long experience) { profile().setExperience(experience); }
    public void giveExperience(long awarded) { profile().addExperience(awarded); }
    public int getPointsUnallocated() { return profile().unallocatedPoints(); }
    public long getHealthTalentPoints() { return profile().talentPoints(Talent.HEALTH); }
    public void setHealthTalentPoints(long value) { profile().setTalentPoints(Talent.HEALTH, value); }
    public long getDamageTalentPoints() { return profile().talentPoints(Talent.DAMAGE); }
    public void setDamageTalentPoints(long value) { profile().setTalentPoints(Talent.DAMAGE, value); }
    public long getMovementSpeedTalentPoints() { return profile().talentPoints(Talent.SPEED); }
    public void setMovementSpeedTalentPoints(long value) { profile().setTalentPoints(Talent.SPEED, value); }
    public long getCritChanceTalentPoints() { return profile().talentPoints(Talent.CRIT_CHANCE); }
    public void setCritChanceTalentPoints(long value) { profile().setTalentPoints(Talent.CRIT_CHANCE, value); }

    public void addCollectedArtifacts(List<String> collection) {
        collection.forEach(profile()::addArtifact);
    }

    public List<String> getCollectedArtifacts() {
        return profile().artifacts().stream().sorted().toList();
    }

    public void sendMessage(String message) {
        Player player = getPlayer();
        if (player != null) player.sendMessage(message);
    }

    public void sendMessage(String message, boolean prefix) {
        sendMessage(prefix ? "[RPG] " + message : message);
    }

    public void resetPoints() {
        for (Talent talent : Talent.values()) {
            profile().setTalentPoints(talent, 0);
        }
    }

    public String getArchetype() { return profile().combatClass().name(); }
    public boolean inCombat() { return false; }

    public void heal() {
        Player player = getPlayer();
        if (player == null) return;
        var maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (maxHealth != null) player.setHealth(maxHealth.getValue());
    }

    public void heal(double amount) {
        Player player = getPlayer();
        if (player == null || amount <= 0) return;
        var maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
        if (maxHealth != null) {
            player.setHealth(Math.min(maxHealth.getValue(), player.getHealth() + amount));
        }
    }

    public void resetCooldowns() {
        // Compatibility no-op. Cooldowns now belong to the systems that own the abilities.
    }
}
