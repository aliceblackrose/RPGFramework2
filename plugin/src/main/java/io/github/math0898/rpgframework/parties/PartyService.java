package io.github.math0898.rpgframework.parties;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public final class PartyService {
    private final Map<UUID, Party> byMember = new HashMap<>();
    private final Map<UUID, UUID> pendingInvites = new HashMap<>();

    public Optional<Party> find(UUID playerId) {
        return Optional.ofNullable(byMember.get(playerId));
    }

    public Party create(UUID leader) {
        Objects.requireNonNull(leader);
        Party existing = byMember.get(leader);
        if (existing != null) {
            return existing;
        }
        Party party = new Party(leader);
        byMember.put(leader, party);
        return party;
    }

    public boolean invite(UUID inviter, UUID target) {
        Party party = create(inviter);
        if (!party.leader().equals(inviter) || byMember.containsKey(target)) {
            return false;
        }
        pendingInvites.put(target, inviter);
        return true;
    }

    public boolean accept(UUID target, boolean bypassLimit) {
        UUID inviter = pendingInvites.remove(target);
        if (inviter == null) {
            return false;
        }
        Party party = byMember.get(inviter);
        if (party == null || byMember.containsKey(target) || !party.add(target, bypassLimit)) {
            return false;
        }
        byMember.put(target, party);
        return true;
    }

    public void decline(UUID target) {
        pendingInvites.remove(target);
    }

    public boolean leave(UUID playerId) {
        Party party = byMember.remove(playerId);
        pendingInvites.remove(playerId);
        if (party == null) {
            return false;
        }
        party.remove(playerId);
        if (party.isEmpty()) {
            removeParty(party);
        }
        return true;
    }

    public boolean kick(UUID leader, UUID target) {
        Party party = byMember.get(leader);
        if (party == null || !party.leader().equals(leader) || target.equals(leader)) {
            return false;
        }
        if (!party.remove(target)) {
            return false;
        }
        byMember.remove(target);
        pendingInvites.remove(target);
        return true;
    }

    public boolean transfer(UUID leader, UUID target) {
        Party party = byMember.get(leader);
        if (party == null || !party.leader().equals(leader) || !party.contains(target)) {
            return false;
        }
        party.transferLeadership(target);
        return true;
    }

    public void removeParty(Party party) {
        party.members().forEach(byMember::remove);
        pendingInvites.entrySet().removeIf(entry ->
                party.contains(entry.getKey()) || party.contains(entry.getValue()));
    }
}
