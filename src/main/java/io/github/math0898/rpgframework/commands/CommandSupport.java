package io.github.math0898.rpgframework.commands;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

final class CommandSupport {
    private CommandSupport() {
    }

    static Player requirePlayer(CommandSourceStack source) {
        if (source.getSender() instanceof Player player) {
            return player;
        }
        source.getSender().sendMessage(Component.text("This command requires a player.", NamedTextColor.RED));
        return null;
    }

    static void error(CommandSourceStack source, String message) {
        source.getSender().sendMessage(Component.text(message, NamedTextColor.RED));
    }

    static void info(CommandSourceStack source, String message) {
        source.getSender().sendMessage(Component.text(message, NamedTextColor.GOLD));
    }
}
