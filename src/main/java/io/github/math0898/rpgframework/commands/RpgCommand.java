package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.RPGFramework;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class RpgCommand implements BasicCommand {
    private final RPGFramework plugin;

    public RpgCommand(RPGFramework plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length == 0) {
            showStatus(source);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "reload" -> reload(source);
            case "xp" -> grantExperience(source, args);
            case "status" -> showStatus(source);
            default -> CommandSupport.error(source, "Usage: /rpg [status|reload|xp <player> <amount>]");
        }
    }

    private void reload(CommandSourceStack source) {
        if (!source.getSender().hasPermission("rpg.admin")) {
            CommandSupport.error(source, "You do not have permission to reload RPGFramework.");
            return;
        }

        plugin.items().reload();
        CommandSupport.info(source, "RPG item definitions reloaded.");
    }

    private void grantExperience(CommandSourceStack source, String[] args) {
        if (!source.getSender().hasPermission("rpg.admin")) {
            CommandSupport.error(source, "You do not have permission to grant RPG experience.");
            return;
        }
        if (args.length != 3) {
            CommandSupport.error(source, "Usage: /rpg xp <player> <amount>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            CommandSupport.error(source, "Target player is not online.");
            return;
        }

        final long amount;
        try {
            amount = Long.parseLong(args[2]);
        } catch (NumberFormatException exception) {
            CommandSupport.error(source, "Experience must be a whole number.");
            return;
        }
        if (amount <= 0L) {
            CommandSupport.error(source, "Experience must be positive.");
            return;
        }

        try {
            plugin.players().addExperience(target, amount);
        } catch (ArithmeticException exception) {
            CommandSupport.error(source, "That experience grant would overflow the profile total.");
            return;
        }

        CommandSupport.info(source, "Granted " + amount + " RPG experience to " + target.getName() + ".");
        target.sendMessage(Component.text(
                "You gained " + amount + " RPG experience.",
                NamedTextColor.GREEN));
    }

    private void showStatus(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("RPGFramework ", NamedTextColor.GOLD)
                .append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.GRAY)));
        source.getSender().sendMessage(Component.text(
                "Loaded profiles: " + plugin.players().loadedProfiles().size()
                        + " | Items: " + plugin.items().ids().size(),
                NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text(
                "/classes, /party, /stats, /rpg-give",
                NamedTextColor.DARK_GRAY));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return source.getSender().hasPermission("rpg.admin")
                    ? List.of("status", "reload", "xp")
                    : List.of("status");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("xp")) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
