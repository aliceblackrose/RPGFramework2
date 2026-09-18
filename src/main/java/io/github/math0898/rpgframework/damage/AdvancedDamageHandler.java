package io.github.math0898.rpgframework.damage;

import io.github.math0898.rpgframework.damage.events.AdvancedDamageEvent;
import io.github.math0898.rpgframework.damage.events.LethalDamageEvent;
import io.github.math0898.rpgframework.damage.events.VerifiedDeathEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public final class AdvancedDamageHandler implements Listener {
    private static final double RPG_TO_VANILLA_SCALE = 5.0;

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        AdvancedDamageEvent advanced = new AdvancedDamageEvent(event);
        Bukkit.getPluginManager().callEvent(advanced);

        if (advanced.isCancelled()) {
            event.setCancelled(true);
            return;
        }

        double rpgDamage = damageCalculation(advanced);
        event.setDamage(rpgDamage / RPG_TO_VANILLA_SCALE);

        if (event.getEntity() instanceof LivingEntity living && living.getHealth() <= event.getFinalDamage()) {
            LethalDamageEvent lethal = new LethalDamageEvent(advanced);
            Bukkit.getPluginManager().callEvent(lethal);
            if (lethal.isCancelled()) {
                event.setCancelled(true);
                return;
            }
            Bukkit.getPluginManager().callEvent(new VerifiedDeathEvent(advanced));
        }
    }

    public static double damageCalculation(AdvancedDamageEvent event) {
        return DamageCalculator.calculate(
                event.getDamages(),
                event.getResistances(),
                event.getPhysicalResistance(),
                event.getMagicResistance());
    }
}
