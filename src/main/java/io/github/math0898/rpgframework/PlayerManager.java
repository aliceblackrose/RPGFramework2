package io.github.math0898.rpgframework;

import io.github.math0898.rpgframework.player.PlayerService;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Deprecated(forRemoval = false)
public final class PlayerManager {
    private static PlayerService service;

    private PlayerManager() {
    }

    static void bind(PlayerService playerService) {
        service = Objects.requireNonNull(playerService);
    }

    static void unbind() {
        service = null;
    }

    public static void init() {
        // Compatibility method. Lifecycle is owned by RPGFramework.
    }

    public static RpgPlayer getPlayer(UUID uuid) {
        PlayerService current = requireService();
        return current.find(uuid).map(ignored -> new RpgPlayer(uuid)).orElse(null);
    }

    public static RpgPlayer getPlayer(String name) {
        PlayerService current = requireService();
        return current.find(name).map(profile -> new RpgPlayer(profile.uuid())).orElse(null);
    }

    public static void addPlayer(RpgPlayer player) {
        Objects.requireNonNull(player, "player");
        Player bukkit = Bukkit.getPlayer(player.getUuid());
        if (bukkit != null && requireService().find(player.getUuid()).isEmpty()) {
            requireService().load(bukkit);
        }
    }

    public static void removePlayer(RpgPlayer player) {
        if (player != null) {
            removePlayer(player.getUuid());
        }
    }

    public static void removePlayer(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            requireService().unload(player);
        }
    }

    public static void scaleHealth(Player player) {
        requireService().find(player.getUniqueId()).ifPresent(profile ->
                RPGFramework.getInstance().classes().apply(player, profile));
    }

    public static void saveAllPlayers() {
        requireService().saveAllBlocking();
    }

    private static PlayerService requireService() {
        return Objects.requireNonNull(service, "RPGFramework is not enabled");
    }
}
