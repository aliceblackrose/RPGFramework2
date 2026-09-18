package io.github.math0898.rpgframework.parties;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.LongSupplier;

public final class PartyService {
    public static final Duration DEFAULT_INVITE_TTL = Duration.ofSeconds(60);

    private final Map<UUID, Party> byMember = new HashMap<>();
    private final Map<UUID, PendingInvite> pendingInvites = new HashMap<>();
    private final Set<UUID> partyChat = new HashSet<>();
    private final LongSupplier ticker;
    private final long inviteTtlNanos;

    public PartyService() {
        this(DEFAULT_INVITE_TTL, System::nanoTime);
    }

    PartyService(Duration inviteTtl, LongSupplier ticker) {
        Objects.requireNonNull(inviteTtl, "inviteTtl");
        this.ticker = Objects.requireNonNull(ticker, "ticker");
        if (inviteTtl.isNegative() || inviteTtl.isZero()) {
            throw new IllegalArgumentException("inviteTtl must be positive");
        }
        try {
            inviteTtlNanos = inviteTtl.toNanos();
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("inviteTtl is too large", exception);
        }
    }

    public synchronized Optional<Party> find(UUID playerId) {
        return Optional.ofNullable(byMember.get(Objects.requireNonNull(playerId, "playerId")));
    }

    public synchronized Party create(UUID leader) {
        Objects.requireNonNull(leader, "leader");
        Party existing = byMember.get(leader);
        if (existing != null) {
            return existing;
        }

        Party party = new Party(leader);
        byMember.put(leader, party);
        return party;
    }

    public synchronized boolean invite(UUID inviter, UUID target) {
        return invite(inviter, target, false);
    }

    public synchronized boolean invite(UUID inviter, UUID target, boolean bypassLimit) {
        Objects.requireNonNull(inviter, "inviter");
        Objects.requireNonNull(target, "target");

        if (inviter.equals(target) || byMember.containsKey(target)) {
            return false;
        }

        Party party = create(inviter);
        if (!party.leader().equals(inviter)) {
            return false;
        }
        if (!bypassLimit && party.isFull()) {
            return false;
        }

        pendingInvites.put(
                target,
                new PendingInvite(inviter, ticker.getAsLong() + inviteTtlNanos, bypassLimit));
        return true;
    }

    public synchronized boolean hasValidInvite(UUID target) {
        PendingInvite invite = validInvite(target);
        return invite != null;
    }

    public synchronized boolean accept(UUID target) {
        return accept(target, false);
    }

    public synchronized boolean accept(UUID target, boolean legacyBypassLimit) {
        Objects.requireNonNull(target, "target");
        PendingInvite invite = validInvite(target);
        if (invite == null) {
            return false;
        }
        pendingInvites.remove(target);

        Party party = byMember.get(invite.inviter());
        if (party == null || byMember.containsKey(target)) {
            return false;
        }

        boolean bypassLimit = invite.bypassLimit() || legacyBypassLimit;
        if (!party.add(target, bypassLimit)) {
            return false;
        }

        byMember.put(target, party);
        return true;
    }

    public synchronized boolean decline(UUID target) {
        return pendingInvites.remove(Objects.requireNonNull(target, "target")) != null;
    }

    public synchronized boolean leave(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        Party party = byMember.remove(playerId);
        pendingInvites.remove(playerId);
        partyChat.remove(playerId);

        if (party == null) {
            return false;
        }

        party.remove(playerId);
        if (party.isEmpty()) {
            removeParty(party);
        }
        return true;
    }

    public synchronized boolean kick(UUID leader, UUID target) {
        Objects.requireNonNull(leader, "leader");
        Objects.requireNonNull(target, "target");
        Party party = byMember.get(leader);
        if (party == null || !party.leader().equals(leader) || target.equals(leader)) {
            return false;
        }
        if (!party.remove(target)) {
            return false;
        }

        byMember.remove(target);
        pendingInvites.remove(target);
        partyChat.remove(target);
        return true;
    }

    public synchronized boolean transfer(UUID leader, UUID target) {
        Objects.requireNonNull(leader, "leader");
        Objects.requireNonNull(target, "target");
        Party party = byMember.get(leader);
        if (party == null || !party.leader().equals(leader) || !party.contains(target)) {
            return false;
        }

        party.transferLeadership(target);
        return true;
    }

    public synchronized boolean disband(UUID leader) {
        Objects.requireNonNull(leader, "leader");
        Party party = byMember.get(leader);
        if (party == null || !party.leader().equals(leader)) {
            return false;
        }

        removeParty(party);
        return true;
    }

    public synchronized boolean togglePartyChat(UUID playerId) {
        Objects.requireNonNull(playerId, "playerId");
        if (!byMember.containsKey(playerId)) {
            partyChat.remove(playerId);
            return false;
        }

        if (!partyChat.add(playerId)) {
            partyChat.remove(playerId);
            return false;
        }
        return true;
    }

    public synchronized boolean partyChatEnabled(UUID playerId) {
        return partyChat.contains(Objects.requireNonNull(playerId, "playerId"))
                && byMember.containsKey(playerId);
    }

    public synchronized void removeParty(Party party) {
        Objects.requireNonNull(party, "party");
        Set<UUID> members = party.members();
        members.forEach(byMember::remove);
        members.forEach(partyChat::remove);
        pendingInvites.entrySet().removeIf(entry ->
                members.contains(entry.getKey()) || members.contains(entry.getValue().inviter()));
    }

    private PendingInvite validInvite(UUID target) {
        PendingInvite invite = pendingInvites.get(target);
        if (invite == null) {
            return null;
        }
        if (ticker.getAsLong() - invite.expiresAtNanos() >= 0L) {
            pendingInvites.remove(target);
            return null;
        }
        return invite;
    }

    private record PendingInvite(UUID inviter, long expiresAtNanos, boolean bypassLimit) {
    }
}
