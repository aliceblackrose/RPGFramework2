package io.github.math0898.rpgframework.player;

import io.github.math0898.rpgframework.RPGFramework;
import io.github.math0898.rpgframework.classes.Classes;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import org.bukkit.configuration.file.YamlConfiguration;

public final class YamlProfileRepository implements ProfileRepository {
    private static final String DATA_VERSION = "4.0";

    private final RPGFramework plugin;
    private final File directory;
    private final ExecutorService ioExecutor;

    public YamlProfileRepository(RPGFramework plugin) {
        this.plugin = plugin;
        directory = new File(plugin.getDataFolder(), "player-data");
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Could not create " + directory);
        }
        ioExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "rpgframework-profile-io");
            thread.setDaemon(true);
            return thread;
        });
    }

    @Override
    public CompletableFuture<PlayerProfile> load(UUID uuid, String currentName) {
        return CompletableFuture.supplyAsync(() -> loadSync(uuid, currentName), ioExecutor);
    }

    @Override
    public CompletableFuture<Void> save(PlayerSnapshot snapshot) {
        return CompletableFuture.runAsync(() -> saveSync(snapshot), ioExecutor);
    }

    private PlayerProfile loadSync(UUID uuid, String currentName) {
        File file = file(uuid);
        if (!file.isFile()) {
            return new PlayerProfile(uuid, currentName);
        }

        try {
            YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
            Classes combatClass = Classes.fromString(yaml.getString("class", "NONE"));
            long experience = Math.max(0L, yaml.getLong("experience", 0L));

            Map<Talent, Long> talents = new EnumMap<>(Talent.class);
            for (Talent talent : Talent.values()) {
                talents.put(talent, yaml.getLong("talents." + talent.storageKey(), 0L));
            }

            Set<String> artifacts = new LinkedHashSet<>(yaml.getStringList("artifacts"));
            return new PlayerProfile(uuid, currentName, combatClass, experience, talents, artifacts);
        } catch (RuntimeException exception) {
            plugin.log(Level.SEVERE, "Failed to load profile " + uuid + "; using defaults.", exception);
            return new PlayerProfile(uuid, currentName);
        }
    }

    private void saveSync(PlayerSnapshot snapshot) {
        YamlConfiguration yaml = new YamlConfiguration();
        yaml.set("version", DATA_VERSION);
        yaml.set("name", snapshot.name());
        yaml.set("class", snapshot.combatClass().name());
        yaml.set("experience", snapshot.experience());
        for (Talent talent : Talent.values()) {
            yaml.set("talents." + talent.storageKey(), snapshot.talentPoints().getOrDefault(talent, 0L));
        }
        yaml.set("artifacts", snapshot.artifacts().stream().sorted().toList());

        try {
            yaml.save(file(snapshot.uuid()));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to save profile " + snapshot.uuid(), exception);
        }
    }

    private File file(UUID uuid) {
        return new File(directory, uuid + ".yml");
    }

    @Override
    public void close() {
        ioExecutor.shutdown();
        try {
            if (!ioExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                ioExecutor.shutdownNow();
            }
        } catch (InterruptedException interrupted) {
            ioExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
