package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.player.PlayerService;
import io.github.math0898.rpgframework.player.StatCalculator;
import io.github.math0898.rpgframework.player.Talent;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
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

        if (args.length == 0) {
            showStats(source, profile);
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "spend" -> spend(source, player, args);
            case "reset" -> {
                players.resetTalents(player);
                CommandSupport.info(source, "All talent points were refunded.");
            }
            default -> CommandSupport.error(source, "Usage: /stats [spend <talent> [points]|reset]");
        }
    }

    private void spend(CommandSourceStack source, org.bukkit.entity.Player player, String[] args) {
        if (args.length < 2 || args.length > 3) {
            CommandSupport.error(source, "Usage: /stats spend <talent> [points]");
            return;
        }

        final Talent talent;
        try {
            talent = Talent.fromInput(args[1]);
        } catch (IllegalArgumentException exception) {
            CommandSupport.error(source, "Unknown talent: " + args[1]);
            return;
        }

        long points = 1L;
        if (args.length == 3) {
            try {
                points = Long.parseLong(args[2]);
            } catch (NumberFormatException exception) {
                CommandSupport.error(source, "Points must be a whole number.");
                return;
            }
        }

        try {
            players.allocateTalent(player, talent, points);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            CommandSupport.error(source, exception.getMessage());
            return;
        }

        CommandSupport.info(source, "Spent " + points + " point(s) on " + talent.displayName() + ".");
    }

    private void showStats(
            CommandSourceStack source,
            io.github.math0898.rpgframework.player.PlayerProfile profile
    ) {
        var calculated = StatCalculator.calculate(profile);

        source.getSender().sendMessage(Component.text("RPG Stats", NamedTextColor.GOLD));
        source.getSender().sendMessage(Component.text(
                "Class: " + profile.combatClass().getName(),
                NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text(
                "Experience: " + profile.experience(),
                NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text(
                "Unallocated points: " + profile.unallocatedPoints(),
                NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text(
                "Max health: " + "%.1f".formatted(calculated.maxHealth()),
                NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text(
                "Damage: x" + "%.2f".formatted(calculated.damageMultiplier())
                        + " +" + "%.1f".formatted(calculated.flatDamage()),
                NamedTextColor.GRAY));
        source.getSender().sendMessage(Component.text(
                "Critical chance: " + "%.1f%%".formatted(calculated.criticalChance() * 100.0),
                NamedTextColor.GRAY));

        for (Talent talent : Talent.values()) {
            source.getSender().sendMessage(Component.text(
                    talent.displayName() + ": " + profile.talentPoints(talent)
                            + " — " + talent.description(),
                    NamedTextColor.DARK_GRAY));
        }
    }

    @Override
    public String permission() {
        return "rpg.stats";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("spend", "reset");
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("spend")) {
            return Arrays.stream(Talent.values())
                    .map(talent -> talent.name().toLowerCase(Locale.ROOT))
                    .toList();
        }
        return List.of();
    }
}
