package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.player.PlayerService;
import io.github.math0898.rpgframework.player.Talent;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class StatsCommand implements BasicCommand {
    private final PlayerService players;

    public StatsCommand(PlayerService players) {
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

        source.getSender().sendMessage(Component.text("RPG Stats", NamedTextColor.GOLD));
        source.getSender().sendMessage(Component.text("Class: " + profile.combatClass().getName(), NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("Experience: " + profile.experience(), NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text("Unallocated points: " + profile.unallocatedPoints(), NamedTextColor.GRAY));
        for (Talent talent : Talent.values()) {
            source.getSender().sendMessage(Component.text(
                    talent.name() + ": " + profile.talentPoints(talent),
                    NamedTextColor.DARK_GRAY));
        }
    }

    @Override
    public String permission() {
        return "rpg.stats";
    }
}
