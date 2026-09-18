package io.github.math0898.rpgframework.player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface ProfileRepository extends AutoCloseable {
    CompletableFuture<PlayerProfile> load(UUID uuid, String currentName);

    CompletableFuture<Void> save(PlayerSnapshot snapshot);

    @Override
    void close();
}
