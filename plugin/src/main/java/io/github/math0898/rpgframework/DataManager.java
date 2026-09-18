package io.github.math0898.rpgframework;

import io.github.math0898.rpgframework.player.PlayerService;
import io.github.math0898.rpgframework.player.ProfileRepository;
import java.util.Objects;

@Deprecated(forRemoval = false)
public final class DataManager {
    private static final DataManager INSTANCE = new DataManager();
    private static ProfileRepository repository;
    private static PlayerService players;

    private DataManager() {
    }

    static void bind(ProfileRepository newRepository, PlayerService playerService) {
        repository = Objects.requireNonNull(newRepository);
        players = Objects.requireNonNull(playerService);
    }

    static void unbind() {
        repository = null;
        players = null;
    }

    public static DataManager getInstance() {
        return INSTANCE;
    }

    public void load(RpgPlayer player) {
        var bukkit = player.getPlayer();
        if (bukkit != null && Objects.requireNonNull(players).find(player.getUuid()).isEmpty()) {
            players.load(bukkit);
        }
    }

    public void save(RpgPlayer player) {
        Objects.requireNonNull(players).find(player.getUuid()).ifPresent(players::save);
    }
}
