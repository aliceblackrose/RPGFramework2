package io.github.math0898.rpgframework.parties;

import io.github.math0898.rpgframework.RPGFramework;
import io.papermc.paper.event.player.AsyncChatEvent;
import java.util.Objects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public final class PartyListener implements Listener {
    private final RPGFramework plugin;
    private final PartyService parties;

    public PartyListener(RPGFramework plugin, PartyService parties) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.parties = Objects.requireNonNull(parties, "parties");
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)
                || !(event.getEntity() instanceof Player victim)) {
            return;
        }

        Party party = parties.find(attacker.getUniqueId()).orElse(null);
        if (party != null && party.contains(victim.getUniqueId())) {
            event.setCancelled(true);
            attacker.sendMessage(Component.text(
                    "You cannot damage a party member.",
                    NamedTextColor.RED));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();
        if (!parties.partyChatEnabled(sender.getUniqueId())) {
            return;
        }

        Component message = event.message();
        event.setCancelled(true);
        Bukkit.getScheduler().runTask(plugin, () -> broadcast(sender, message));
    }

    public void broadcast(Player sender, Component message) {
        Party party = parties.find(sender.getUniqueId()).orElse(null);
        if (party == null) {
            return;
        }

        Component rendered = Component.text("[Party] ", NamedTextColor.AQUA)
                .append(Component.text(sender.getName() + ": ", NamedTextColor.WHITE))
                .append(message);

        party.members().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(member -> member.sendMessage(rendered));
    }
}
