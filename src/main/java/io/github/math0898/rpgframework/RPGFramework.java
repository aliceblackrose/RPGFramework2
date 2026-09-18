package io.github.math0898.rpgframework;

import io.github.math0898.rpgframework.classes.ClassService;
import io.github.math0898.rpgframework.commands.ClassesCommand;
import io.github.math0898.rpgframework.commands.GiveCommand;
import io.github.math0898.rpgframework.commands.PartyCommand;
import io.github.math0898.rpgframework.commands.RpgCommand;
import io.github.math0898.rpgframework.commands.StatsCommand;
import io.github.math0898.rpgframework.damage.AdvancedDamageHandler;
import io.github.math0898.rpgframework.items.ItemManager;
import io.github.math0898.rpgframework.items.ItemRegistry;
import io.github.math0898.rpgframework.parties.PartyManager;
import io.github.math0898.rpgframework.parties.PartyService;
import io.github.math0898.rpgframework.player.PlayerLifecycleListener;
import io.github.math0898.rpgframework.player.PlayerService;
import io.github.math0898.rpgframework.player.YamlProfileRepository;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("deprecation")
public final class RPGFramework extends JavaPlugin {
    private static RPGFramework instance;
    private YamlProfileRepository profileRepository;
    private PlayerService playerService;
    private ClassService classService;
    private PartyService partyService;
    private ItemRegistry itemRegistry;

    @Override
    public void onEnable() {
        instance = this;
        profileRepository = new YamlProfileRepository(this);
        classService = new ClassService();
        playerService = new PlayerService(this, profileRepository, classService);
        partyService = new PartyService();
        itemRegistry = new ItemRegistry(this);

        PlayerManager.bind(playerService);
        DataManager.bind(profileRepository, playerService);
        PartyManager.bind(partyService);
        ItemManager.bind(itemRegistry);

        itemRegistry.reload();
        registerListeners();
        registerCommands();
        Bukkit.getOnlinePlayers().forEach(playerService::load);
        getLogger().info("RPGFramework enabled on Paper 26.2 / Java 25.");
    }

    @Override
    public void onDisable() {
        if (playerService != null) playerService.saveAllBlocking();
        if (profileRepository != null) profileRepository.close();
        ItemManager.unbind();
        PartyManager.unbind();
        DataManager.unbind();
        PlayerManager.unbind();
        instance = null;
    }

    private void registerListeners() {
        var manager = Bukkit.getPluginManager();
        manager.registerEvents(new PlayerLifecycleListener(playerService), this);
        manager.registerEvents(new AdvancedDamageHandler(), this);
        manager.registerEvents(PartyManager.listener(), this);
    }

    private void registerCommands() {
        registerCommand("rpg", "RPGFramework root command", List.of(), new RpgCommand(this));
        registerCommand("classes", "Select or inspect your RPG class", List.of(), new ClassesCommand(playerService));
        registerCommand("party", "Create and manage RPG parties", List.of(), new PartyCommand(partyService));
        registerCommand("rpg-give", "Give registered RPG items", List.of(), new GiveCommand(itemRegistry));
        registerCommand("stats", "Display RPG stats", List.of(), new StatsCommand(playerService));
        registerCommand("rpg-debug", "RPGFramework diagnostics", List.of(), new RpgCommand(this));
        registerCommand("artifact", "RPG artifact status", List.of(), new RpgCommand(this));
        registerCommand("tutorial", "RPG help", List.of(), new RpgCommand(this));
        registerCommand("updates", "RPG framework version information", List.of(), new RpgCommand(this));
    }

    public static RPGFramework getInstance() {
        return Objects.requireNonNull(instance, "RPGFramework is not enabled");
    }

    public PlayerService players() { return Objects.requireNonNull(playerService); }
    public ClassService classes() { return Objects.requireNonNull(classService); }
    public PartyService parties() { return Objects.requireNonNull(partyService); }
    public ItemRegistry items() { return Objects.requireNonNull(itemRegistry); }

    public void log(Level level, String message, Throwable throwable) {
        getLogger().log(level, message, throwable);
    }
}
