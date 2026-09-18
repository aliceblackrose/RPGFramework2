package io.github.math0898.rpgframework.damage;

import io.github.math0898.rpgframework.RPGFramework;
import io.github.math0898.rpgframework.damage.events.AdvancedDamageEvent;
import io.github.math0898.rpgframework.damage.events.LethalDamageEvent;
import io.github.math0898.rpgframework.damage.events.VerifiedDeathEvent;
import io.github.math0898.rpgframework.player.CombatService;
import io.github.math0898.rpgframework.player.PlayerService;
import io.github.math0898.rpgframework.player.StatCalculator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.DoubleSupplier;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public final class AdvancedDamageHandler implements Listener {
    private static final double RPG_TO_VANILLA_SCALE = 5.0;
    private static final double CRITICAL_MULTIPLIER = 2.0;

    private final RPGFramework plugin;
    private final PlayerService players;
    private final CombatService combat;
    private final DoubleSupplier criticalRoll;
    private final Map<UUID, AdvancedDamageEvent> pendingDeaths = new HashMap<>();

    public AdvancedDamageHandler(
            RPGFramework plugin,
            PlayerService players,
            CombatService combat
    ) {
        this(plugin, players, combat, () -> ThreadLocalRandom.current().nextDouble());
    }

    AdvancedDamageHandler(
            RPGFramework plugin,
            PlayerService players,
            CombatService combat,
            DoubleSupplier criticalRoll
    ) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.players = Objects.requireNonNull(players, "players");
        this.combat = Objects.requireNonNull(combat, "combat");
        this.criticalRoll = Objects.requireNonNull(criticalRoll, "criticalRoll");
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        AdvancedDamageEvent advanced = new AdvancedDamageEvent(event);
        applyAttackerStats(advanced);
        Bukkit.getPluginManager().callEvent(advanced);

        if (advanced.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        double rpgDamage = damageCalculation(advanced);
        event.setDamage(rpgDamage / RPG_TO_VANILLA_SCALE);
        markCombat(event);

        if (!(event.getEntity() instanceof LivingEntity living)
                || living.getHealth() > event.getFinalDamage()) {
            return;
        }

        LethalDamageEvent lethal = new LethalDamageEvent(advanced);
        Bukkit.getPluginManager().callEvent(lethal);
        if (lethal.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        UUID entityId = living.getUniqueId();
        pendingDeaths.put(entityId, advanced);
        Bukkit.getScheduler().runTask(plugin, () -> pendingDeaths.remove(entityId, advanced));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        AdvancedDamageEvent source = pendingDeaths.remove(event.getEntity().getUniqueId());
        if (source != null) {
            Bukkit.getPluginManager().callEvent(new VerifiedDeathEvent(source));
        }
    }

    public static double damageCalculation(AdvancedDamageEvent event) {
        return DamageCalculator.calculate(
                event.getDamages(),
                event.getResistances(),
                event.getPhysicalResistance(),
                event.getMagicResistance());
    }

    private void applyAttackerStats(AdvancedDamageEvent event) {
        Player attacker = resolvePlayerDamager(event.getBasicEvent());
        if (attacker == null) {
            return;
        }

        players.find(attacker.getUniqueId()).ifPresent(profile -> {
            var stats = StatCalculator.calculate(profile);
            DamageType primary = event.getPrimaryDamage();
            double damage = (event.getDamage(primary) * stats.damageMultiplier()) + stats.flatDamage();

            if (stats.criticalChance() > 0.0 && criticalRoll.getAsDouble() < stats.criticalChance()) {
                damage *= CRITICAL_MULTIPLIER;
                event.setCritical(true);
            }

            event.setDamage(primary, damage);
        });
    }

    private void markCombat(EntityDamageEvent event) {
        if (event.getFinalDamage() <= 0.0 || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        Player attacker = resolvePlayerDamager(event);
        if (attacker == null || attacker.getUniqueId().equals(victim.getUniqueId())) {
            combat.markCombat(victim.getUniqueId());
            return;
        }

        combat.markCombat(attacker.getUniqueId(), victim.getUniqueId());
    }

    private static Player resolvePlayerDamager(EntityDamageEvent event) {
        if (!(event instanceof EntityDamageByEntityEvent byEntity)) {
            return null;
        }

        if (byEntity.getDamager() instanceof Player player) {
            return player;
        }

        if (byEntity.getDamager() instanceof Projectile projectile
                && projectile.getShooter() instanceof Player player) {
            return player;
        }

        return null;
    }
}
