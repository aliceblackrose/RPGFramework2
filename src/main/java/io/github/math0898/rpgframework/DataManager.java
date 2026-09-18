package io.github.math0898.rpgframework;

import io.github.math0898.rpgframework.player.PlayerService;
import java.util.Objects;

@Deprecated(forRemoval = false)
public final class DataManager {
    private static final DataManager INSTANCE = new DataManager();
    private static PlayerService players;

    private DataManager() {
    }

    static void bind(PlayerService playerService) {
        players = Objects.requireNonNull(playerService, "playerService");
    }

    static void unbind() {
        players = null;
    }

    public static DataManager getInstance() {
        return INSTANCE;
    }

    public void load(RpgPlayer player) {
        Objects.requireNonNull(player, "player");
        var bukkit = player.getPlayer();
        PlayerService service = requirePlayers();
        if (bukkit != null && service.find(player.getUuid()).isEmpty()) {
            service.load(bukkit);
        }
    }

    public void save(RpgPlayer player) {
        Objects.requireNonNull(player, "player");
        PlayerService service = requirePlayers();
        service.find(player.getUuid()).ifPresent(service::save);
    }

    private static PlayerService requirePlayers() {
        return Objects.requireNonNull(players, "RPGFramework is not enabled");
    }
}
