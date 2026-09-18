package io.github.math0898.rpgframework.parties;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class Party {
    public static final int DEFAULT_MAX_SIZE = 6;

    private UUID leader;
    private final LinkedHashSet<UUID> members = new LinkedHashSet<>();

    public Party(UUID leader) {
        this.leader = Objects.requireNonNull(leader);
        members.add(leader);
    }

    public UUID leader() {
        return leader;
    }

    public Set<UUID> members() {
        return Set.copyOf(members);
    }

    public boolean contains(UUID playerId) {
        return members.contains(playerId);
    }

    public int size() {
        return members.size();
    }

    public boolean add(UUID playerId, boolean bypassLimit) {
        Objects.requireNonNull(playerId);
        if (members.contains(playerId)) {
            return false;
        }
        if (!bypassLimit && members.size() >= DEFAULT_MAX_SIZE) {
            return false;
        }
        return members.add(playerId);
    }

    public boolean remove(UUID playerId) {
        if (!members.remove(playerId)) {
            return false;
        }
        if (playerId.equals(leader) && !members.isEmpty()) {
            leader = members.iterator().next();
        }
        return true;
    }

    public boolean isEmpty() {
        return members.isEmpty();
    }

    public void transferLeadership(UUID playerId) {
        if (!members.contains(playerId)) {
            throw new IllegalArgumentException("New leader is not in the party");
        }
        leader = playerId;
    }
}
