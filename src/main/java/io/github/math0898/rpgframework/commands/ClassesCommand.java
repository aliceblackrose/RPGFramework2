package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.classes.Classes;
import io.github.math0898.rpgframework.player.PlayerService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class ClassesCommand implements BasicCommand {
    private final PlayerService players;

    public ClassesCommand(PlayerService players) {
        this.players = players;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        var player = CommandSupport.requirePlayer(source);
        if (player == null) {
            return;
        }

        var profile = players.find(player.getUniqueId()).orElse(null);
        if (profile == null) {
            CommandSupport.error(source, "Your RPG profile is still loading.");
            return;
        }

        if (args.length == 0) {
            source.getSender().sendMessage(Component.text(
                    "Current class: " + profile.combatClass().getName(),
                    NamedTextColor.GOLD));
            source.getSender().sendMessage(Component.text(
                    "Use /classes list or /classes <class>.",
                    NamedTextColor.GRAY));
            return;
        }

        if (args[0].equalsIgnoreCase("list")) {
            listClasses(source);
            return;
        }

        Classes selected = Classes.fromString(args[0]);
        if (selected == Classes.NONE && !args[0].equalsIgnoreCase("none")) {
            CommandSupport.error(source, "Unknown class: " + args[0]);
            return;
        }

        players.changeClass(player, selected);
        source.getSender().sendMessage(Component.text(
                "Class changed to " + selected.getName() + ".",
                NamedTextColor.GREEN));
    }

    private static void listClasses(CommandSourceStack source) {
        source.getSender().sendMessage(Component.text("Available RPG classes", NamedTextColor.GOLD));
        Arrays.stream(Classes.values())
                .filter(value -> value != Classes.NONE)
                .forEach(value -> source.getSender().sendMessage(Component.text(
                        value.getName()
                                + " — health +" + value.healthBonus()
                                + ", damage x" + "%.2f".formatted(value.damageMultiplier())
                                + ", speed " + "%+.0f%%".formatted(value.speedBonus() * 100.0),
                        NamedTextColor.GRAY)));
    }

    @Override
    public String permission() {
        return "rpg.classes";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return java.util.stream.Stream.concat(
                            java.util.stream.Stream.of("list"),
                            Arrays.stream(Classes.values())
                                    .map(value -> value.name().toLowerCase(Locale.ROOT)))
                    .toList();
        }
        return java.util.List.of();
    }
}
