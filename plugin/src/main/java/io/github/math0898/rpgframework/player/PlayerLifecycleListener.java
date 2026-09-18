package io.github.math0898.rpgframework.player;

import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLifecycleListener implements Listener {
    private final PlayerService players;

    public PlayerLifecycleListener(PlayerService players) {
        this.players = Objects.requireNonNull(players);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        players.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        players.unload(event.getPlayer());
    }
}
