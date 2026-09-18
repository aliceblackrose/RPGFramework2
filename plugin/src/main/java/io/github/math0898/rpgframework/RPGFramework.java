package io.github.math0898.rpgframework;

import io.github.math0898.rpgframework.commands.*;
import io.github.math0898.rpgframework.commands.stats.StatsCommand;
import io.github.math0898.rpgframework.damage.AdvancedDamageHandler;
import io.github.math0898.rpgframework.enemies.MobManager;
import io.github.math0898.rpgframework.hooks.HookManager;
import io.github.math0898.rpgframework.items.ItemManager;
import io.github.math0898.rpgframework.parties.PartyManager;
import io.github.math0898.rpgframework.systems.GodEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import sugaku.rpg.framework.RPGEventListener;
import sugaku.rpg.mobs.teir1.eiryeras.EiryerasBoss;
import sugaku.rpg.mobs.teir1.feyrith.FeyrithBoss;

import java.util.logging.Level;

/**
 * The main class for the RPG Framework. A few methods and what not will be declared here for calling from other plugins
 * that are a part of the RPG suite.
 *
 * @author Sugaku
 */
public final class RPGFramework extends JavaPlugin implements Listener {

    /**
     * A pointer to the plugin instance.
     */
    public static RPGFramework plugin = null;

    /**
     * Is holographic displays enabled on the server?
     */
    public static boolean useHolographicDisplays = false;

    /**
     * Is decent holograms on the server?
     */
    public static boolean useDecentHolograms = false;


    /**
     * This method sends a message to the console and infers the level it should be sent at.
     *
     * @param message The message to send to the console.
     * @param color The main color of the message being sent.
     */
    public static void console (String message, ChatColor color) {
        switch (color) {
            case RED -> console(message, color, Level.SEVERE);
            case YELLOW -> console(message, color, Level.WARNING);
            default -> console(message, color, Level.INFO);
        }
    }

    /**
     * This method sends a message to the console.
     *
     * @param message The message to send to the console.
     * @param color The main color of the message being sent.
     * @param lvl The level that the message should be sent at.
     */
    public static void console (String message, ChatColor color, Level lvl) {
        plugin.getLogger().log(lvl, color + message);
    }

    /**
     * An accessor method to the active Plugin instance.
     *
     * @return The active Plugin instance.
     */
    public static RPGFramework getInstance () {
        return plugin;
    }

    /**
     * Called on enable. Just the normal things such as loading the config, registering listeners, initializing methods.
     */
    @Override
    public void onEnable () {
        long startTime = System.currentTimeMillis();
        plugin = this;

        //Register damage listeners
        Bukkit.getPluginManager().registerEvents(new AdvancedDamageHandler(), this);
        Bukkit.getPluginManager().registerEvents(new GodEventListener(), this);
        PartyManager.init();
        PlayerManager.init();
        DataManager.getInstance();
        registerCommands();

        //Establish hooks
        HookManager.getInstance();
        useHolographicDisplays = Bukkit.getPluginManager().isPluginEnabled("HolographicDisplays");
        useDecentHolograms = Bukkit.getPluginManager().isPluginEnabled("DecentHolograms");
        if (!useHolographicDisplays && !useDecentHolograms) {
            console("Holographic Displays nor Decent Holograms was not found.", ChatColor.YELLOW);
            console("This is non fatal error however you will not see damage numbers when you hit mobs.", ChatColor.YELLOW);
        }
        ItemManager.getInstance();
        MobManager.getInstance();

        // Register legacy listeners still required by the current mob/event implementation.
        Bukkit.getPluginManager().registerEvents(new RPGEventListener(), plugin);
        Bukkit.getPluginManager().registerEvents(new EiryerasBoss(), plugin);
        Bukkit.getPluginManager().registerEvents(new FeyrithBoss(), plugin);

        console("Plugin enabled! " + ChatColor.DARK_GRAY + "Took: " + (System.currentTimeMillis() - startTime) + "ms", ChatColor.GREEN);
    }

    /**
     * Registers all the commands in a simple group.
     */
    private void registerCommands () {
        console("Registering commands.", ChatColor.GRAY);
        new Tutorial();
        new Updates();
        new Classes();
        new SummonRPG();
        new GiveCommand();
        new StatsCommand();
        new PartyCommand();
        new DebugCommand();
        new EditorCommand();
        new ArtifactCommand();
        new DungeonCreateCommand();
        console("Commands registered.", ChatColor.GREEN);
    }
}
