package io.github.math0898.rpgframework.parties;

import io.github.math0898.rpgframework.RPGFramework;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

@Deprecated(forRemoval = false)
public final class PartyManager {
    private static PartyService service;

    private PartyManager() {
    }

    public static void init() {
        // Lifecycle is owned by RPGFramework.
    }

    public static void bind(PartyService partyService) {
        service = Objects.requireNonNull(partyService, "partyService");
    }

    public static void unbind() {
        service = null;
    }

    public static Listener listener() {
        return new PartyListener(RPGFramework.getInstance(), requireService());
    }

    public static void addParty(Party party) {
        Objects.requireNonNull(party, "party");
        PartyService current = requireService();
        current.create(party.leader());
        for (UUID member : party.members()) {
            if (!member.equals(party.leader())) {
                current.invite(party.leader(), member, true);
                current.accept(member);
            }
        }
    }

    public static void removeParty(Party party) {
        requireService().removeParty(party);
    }

    public static Party findParty(Player player) {
        return requireService().find(Objects.requireNonNull(player, "player").getUniqueId()).orElse(null);
    }

    public static void togglePartyChat(Player player) {
        requireService().togglePartyChat(Objects.requireNonNull(player, "player").getUniqueId());
    }

    private static PartyService requireService() {
        return Objects.requireNonNull(service, "RPGFramework is not enabled");
    }
}
