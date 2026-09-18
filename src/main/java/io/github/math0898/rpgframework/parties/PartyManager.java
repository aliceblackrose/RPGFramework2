package io.github.math0898.rpgframework.parties;

import io.github.math0898.rpgframework.RPGFramework;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

@Deprecated(forRemoval = false)
public final class PartyManager {
    private static PartyService service;
    private static final Set<UUID> PARTY_CHAT = ConcurrentHashMap.newKeySet();
    private static final Listener LISTENER = new PartyListener();

    private PartyManager() {
    }

    public static void init() {
    }

    public static void bind(PartyService partyService) {
        service = Objects.requireNonNull(partyService);
    }

    public static void unbind() {
        service = null;
        PARTY_CHAT.clear();
    }

    public static Listener listener() {
        return LISTENER;
    }

    public static void addParty(Party party) {
        Objects.requireNonNull(party);
        PartyService current = requireService();
        current.create(party.leader());
        for (UUID member : party.members()) {
            if (!member.equals(party.leader())) {
                current.invite(party.leader(), member);
                current.accept(member, true);
            }
        }
    }

    public static void removeParty(Party party) {
        requireService().removeParty(party);
    }

    public static Party findParty(Player player) {
        return requireService().find(player.getUniqueId()).orElse(null);
    }

    public static void togglePartyChat(Player player) {
        UUID uuid = player.getUniqueId();
        if (!PARTY_CHAT.add(uuid)) {
            PARTY_CHAT.remove(uuid);
        }
    }

    private static PartyService requireService() {
        return Objects.requireNonNull(service, "RPGFramework is not enabled");
    }

    private static final class PartyListener implements Listener {
        @EventHandler(ignoreCancelled = true)
        public void onDamage(EntityDamageByEntityEvent event) {
            if (!(event.getDamager() instanceof Player attacker)
                    || !(event.getEntity() instanceof Player victim)) {
                return;
            }
            Party party = findParty(attacker);
            if (party != null && party.contains(victim.getUniqueId())) {
                event.setCancelled(true);
                attacker.sendMessage(Component.text("You cannot damage a party member.", NamedTextColor.RED));
            }
        }
    }
}
