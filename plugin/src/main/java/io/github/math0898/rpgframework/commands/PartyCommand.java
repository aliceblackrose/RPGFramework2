package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.parties.Party;
import io.github.math0898.rpgframework.parties.PartyService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PartyCommand implements BasicCommand {
    private final PartyService parties;

    public PartyCommand(PartyService parties) {
        this.parties = parties;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = CommandSupport.requirePlayer(source);
        if (player == null) {
            return;
        }
        UUID self = player.getUniqueId();

        if (args.length == 0) {
            showParty(source, self);
            return;
        }

        switch (args[0].toLowerCase(java.util.Locale.ROOT)) {
            case "invite" -> invite(source, player, args);
            case "accept" -> accept(source, player);
            case "decline" -> {
                parties.decline(self);
                CommandSupport.info(source, "Pending party invite declined.");
            }
            case "leave" -> CommandSupport.info(source,
                    parties.leave(self) ? "You left your party." : "You are not in a party.");
            case "kick" -> kick(source, player, args);
            case "leader" -> transfer(source, player, args);
            case "chat" -> chat(source, player, args);
            default -> CommandSupport.error(source,
                    "Usage: /party [invite|accept|decline|leave|kick|leader|chat]");
        }
    }

    private void invite(CommandSourceStack source, Player player, String[] args) {
        if (args.length != 2) {
            CommandSupport.error(source, "Usage: /party invite <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || target.equals(player)) {
            CommandSupport.error(source, "That player is not available.");
            return;
        }
        if (!parties.invite(player.getUniqueId(), target.getUniqueId())) {
            CommandSupport.error(source, "Could not invite that player.");
            return;
        }
        target.sendMessage(Component.text(
                player.getName() + " invited you to a party. Use /party accept.",
                NamedTextColor.GOLD));
        CommandSupport.info(source, "Party invite sent.");
    }

    private void accept(CommandSourceStack source, Player player) {
        boolean bypass = player.hasPermission("rpg.parties.maxbypass");
        CommandSupport.info(source, parties.accept(player.getUniqueId(), bypass)
                ? "Party invite accepted."
                : "You have no valid party invite.");
    }

    private void kick(CommandSourceStack source, Player player, String[] args) {
        if (args.length != 2) {
            CommandSupport.error(source, "Usage: /party kick <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !parties.kick(player.getUniqueId(), target.getUniqueId())) {
            CommandSupport.error(source, "Could not kick that player.");
            return;
        }
        target.sendMessage(Component.text("You were removed from the party.", NamedTextColor.RED));
        CommandSupport.info(source, "Player removed from the party.");
    }

    private void transfer(CommandSourceStack source, Player player, String[] args) {
        if (args.length != 2) {
            CommandSupport.error(source, "Usage: /party leader <player>");
            return;
        }
        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || !parties.transfer(player.getUniqueId(), target.getUniqueId())) {
            CommandSupport.error(source, "Could not transfer leadership.");
            return;
        }
        CommandSupport.info(source, "Party leadership transferred to " + target.getName() + ".");
    }

    private void chat(CommandSourceStack source, Player player, String[] args) {
        if (args.length < 2) {
            CommandSupport.error(source, "Usage: /party chat <message>");
            return;
        }
        Party party = parties.find(player.getUniqueId()).orElse(null);
        if (party == null) {
            CommandSupport.error(source, "You are not in a party.");
            return;
        }
        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Component rendered = Component.text("[Party] " + player.getName() + ": ", NamedTextColor.AQUA)
                .append(Component.text(message, NamedTextColor.WHITE));
        party.members().stream()
                .map(Bukkit::getPlayer)
                .filter(java.util.Objects::nonNull)
                .forEach(member -> member.sendMessage(rendered));
    }

    private void showParty(CommandSourceStack source, UUID self) {
        Party party = parties.find(self).orElse(null);
        if (party == null) {
            CommandSupport.info(source, "You are not in a party.");
            return;
        }
        String members = party.members().stream()
                .map(uuid -> {
                    Player online = Bukkit.getPlayer(uuid);
                    return online == null ? uuid.toString() : online.getName();
                })
                .collect(java.util.stream.Collectors.joining(", "));
        CommandSupport.info(source, "Party members: " + members);
    }

    @Override
    public String permission() {
        return "rpg.parties";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("invite", "accept", "decline", "leave", "kick", "leader", "chat");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("invite")
                || args[0].equalsIgnoreCase("kick")
                || args[0].equalsIgnoreCase("leader"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
