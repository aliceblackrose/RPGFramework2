package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.RPGFramework;
import io.github.math0898.rpgframework.classes.ClassService;
import io.github.math0898.rpgframework.classes.Classes;
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
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.repository = Objects.requireNonNull(repository, "repository");
        this.classService = Objects.requireNonNull(classService, "classService");
    }

    public void load(Player player) {
        Objects.requireNonNull(player, "player");
        UUID uuid = player.getUniqueId();
        String name = player.getName();

        repository.load(uuid, name).whenComplete((loadedProfile, error) ->
                Bukkit.getScheduler().runTask(plugin, () -> finishLoad(uuid, name, loadedProfile, error)));
    }

    private void finishLoad(UUID uuid, String requestedName, PlayerProfile loadedProfile, Throwable error) {
        PlayerProfile profile = loadedProfile;
        if (error != null) {
            plugin.log(Level.SEVERE, "Could not load " + requestedName, error);
            profile = new PlayerProfile(uuid, requestedName);
        }

        Player current = Bukkit.getPlayer(uuid);
        if (current == null || !current.isOnline()) {
            repository.save(profile.snapshot()).exceptionally(saveError -> {
                plugin.log(Level.SEVERE, "Could not save offline profile " + requestedName, saveError);
                return null;
            });
            return;
        }

        profile.updateName(current.getName());
        profiles.put(uuid, profile);
        classService.apply(current, profile);
    }

    public void unload(Player player) {
        Objects.requireNonNull(player, "player");
        PlayerProfile profile = profiles.remove(player.getUniqueId());
        if (profile == null) {
            return;
        }

        profile.updateName(player.getName());
        save(profile);
    }

    public Optional<PlayerProfile> find(UUID uuid) {
        return Optional.ofNullable(profiles.get(Objects.requireNonNull(uuid, "uuid")));
    }

    public Optional<PlayerProfile> find(String playerName) {
        if (playerName == null || playerName.isBlank()) {
            return Optional.empty();
        }
        return profiles.values().stream()
                .filter(profile -> profile.name().equalsIgnoreCase(playerName))
                .findFirst();
    }

    public PlayerProfile require(Player player) {
        Objects.requireNonNull(player, "player");
        return find(player.getUniqueId()).orElseThrow(
                () -> new IllegalStateException("Profile is not loaded for " + player.getName()));
    }

    public void changeClass(Player player, Classes type) {
        Objects.requireNonNull(type, "type");
        PlayerProfile profile = require(player);
        profile.combatClass(type);
        classService.apply(player, profile);
        save(profile);
    }

    public void addExperience(Player player, long amount) {
        PlayerProfile profile = require(player);
        profile.addExperience(amount);
        save(profile);
    }

    public void allocateTalent(Player player, Talent talent, long points) {
        PlayerProfile profile = require(player);
        profile.allocateTalent(talent, points);
        classService.apply(player, profile);
        save(profile);
    }

    public void resetTalents(Player player) {
        PlayerProfile profile = require(player);
        profile.resetTalents();
        classService.apply(player, profile);
        save(profile);
    }

    public Collection<PlayerProfile> loadedProfiles() {
        return java.util.List.copyOf(profiles.values());
    }

    public void save(PlayerProfile profile) {
        Objects.requireNonNull(profile, "profile");
        repository.save(profile.snapshot()).exceptionally(error -> {
            plugin.log(Level.SEVERE, "Could not save " + profile.name(), error);
            return null;
        });
    }

    public void saveAll() {
        for (PlayerProfile profile : profiles.values()) {
            save(profile);
        }
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
