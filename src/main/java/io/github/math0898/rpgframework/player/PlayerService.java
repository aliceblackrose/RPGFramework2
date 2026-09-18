package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.RPGFramework;
import io.github.math0898.rpgframework.classes.ClassService;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PlayerService {
    private final RPGFramework plugin;
    private final ProfileRepository repository;
    private final ClassService classService;
    private final Map<UUID, PlayerProfile> profiles = new ConcurrentHashMap<>();

    public PlayerService(RPGFramework plugin, ProfileRepository repository, ClassService classService) {
        this.plugin = Objects.requireNonNull(plugin);
        this.repository = Objects.requireNonNull(repository);
        this.classService = Objects.requireNonNull(classService);
    }

    public void load(Player player) {
        UUID uuid = player.getUniqueId();
        String name = player.getName();
        repository.load(uuid, name).whenComplete((loadedProfile, error) ->
                Bukkit.getScheduler().runTask(plugin, () -> {
                    PlayerProfile profile = loadedProfile;
                    if (error != null) {
                        plugin.log(Level.SEVERE, "Could not load " + name, error);
                        profile = new PlayerProfile(uuid, name);
                    }
                    Player current = Bukkit.getPlayer(uuid);
                    if (current == null || !current.isOnline()) {
                        repository.save(profile.snapshot());
                        return;
                    }
                    profile.updateName(current.getName());
                    profiles.put(uuid, profile);
                    classService.apply(current, profile);
                }));
    }

    public void unload(Player player) {
        PlayerProfile profile = profiles.remove(player.getUniqueId());
        if (profile == null) {
            return;
        }
        profile.updateName(player.getName());
        repository.save(profile.snapshot()).exceptionally(error -> {
            plugin.log(Level.SEVERE, "Could not save " + profile.name(), error);
            return null;
        });
    }

    public Optional<PlayerProfile> find(UUID uuid) {
        return Optional.ofNullable(profiles.get(uuid));
    }

    public Optional<PlayerProfile> find(String playerName) {
        if (playerName == null) {
            return Optional.empty();
        }
        return profiles.values().stream()
                .filter(profile -> profile.name().equalsIgnoreCase(playerName))
                .findFirst();
    }

    public PlayerProfile require(Player player) {
        return find(player.getUniqueId()).orElseThrow(
                () -> new IllegalStateException("Profile is not loaded for " + player.getName()));
    }

    public void changeClass(Player player, io.github.math0898.rpgframework.classes.Classes type) {
        PlayerProfile profile = require(player);
        profile.combatClass(type);
        classService.apply(player, profile);
    }

    public Collection<PlayerProfile> loadedProfiles() {
        return java.util.List.copyOf(profiles.values());
    }

    public void save(PlayerProfile profile) {
        repository.save(profile.snapshot()).exceptionally(error -> {
            plugin.log(Level.SEVERE, "Could not save " + profile.name(), error);
            return null;
        });
    }

    public void saveAllBlocking() {
        var futures = new ArrayList<java.util.concurrent.CompletableFuture<Void>>();
        for (PlayerProfile profile : profiles.values()) {
            futures.add(repository.save(profile.snapshot()));
        }
        for (var future : futures) {
            try {
                future.get(10, TimeUnit.SECONDS);
            } catch (Exception exception) {
                plugin.log(Level.SEVERE, "Failed to flush a player profile during shutdown.", exception);
            }
        }
        profiles.clear();
    }

}
