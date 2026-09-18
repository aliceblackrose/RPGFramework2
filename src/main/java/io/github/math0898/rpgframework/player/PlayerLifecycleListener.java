package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.CooldownService;
import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public final class PlayerLifecycleListener implements Listener {
    private final PlayerService players;
    private final CombatService combat;
    private final CooldownService cooldowns;

    public PlayerLifecycleListener(
            PlayerService players,
            CombatService combat,
            CooldownService cooldowns
    ) {
        this.players = Objects.requireNonNull(players, "players");
        this.combat = Objects.requireNonNull(combat, "combat");
        this.cooldowns = Objects.requireNonNull(cooldowns, "cooldowns");
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        players.load(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        players.unload(player);
        combat.clear(player.getUniqueId());
        cooldowns.clear(player.getUniqueId());
    }
}
