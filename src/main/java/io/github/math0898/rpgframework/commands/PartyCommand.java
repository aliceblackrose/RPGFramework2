package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.parties.Party;
import io.github.math0898.rpgframework.parties.PartyService;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class PartyCommand implements BasicCommand {
    private final PartyService parties;

    public PartyCommand(PartyService parties) {
        this.parties = Objects.requireNonNull(parties, "parties");
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        Player player = CommandSupport.requirePlayer(source);
        if (player == null) {
            return;
        }

        if (args.length == 0) {
            showParty(source, player.getUniqueId());
            return;
        }

        switch (args[0].toLowerCase(Locale.ROOT)) {
            case "create" -> create(source, player);
            case "invite" -> invite(source, player, args);
            case "accept" -> accept(source, player);
            case "decline" -> decline(source, player);
            case "leave" -> leave(source, player);
            case "disband" -> disband(source, player);
            case "kick" -> kick(source, player, args);
            case "leader" -> transfer(source, player, args);
            case "chat" -> chat(source, player, args);
            default -> CommandSupport.error(source,
                    "Usage: /party [create|invite|accept|decline|leave|disband|kick|leader|chat]");
        }
    }

    private void create(CommandSourceStack source, Player player) {
        Party existing = parties.find(player.getUniqueId()).orElse(null);
        if (existing != null) {
            CommandSupport.error(source, "You are already in a party.");
            return;
        }
        parties.create(player.getUniqueId());
        CommandSupport.info(source, "Party created.");
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

        boolean bypassLimit = player.hasPermission("rpg.parties.maxbypass");
        if (!parties.invite(player.getUniqueId(), target.getUniqueId(), bypassLimit)) {
            CommandSupport.error(source, "Could not invite that player. Only the party leader can invite.");
            return;
        }

        target.sendMessage(Component.text(
                player.getName() + " invited you to a party. Use /party accept within 60 seconds.",
                NamedTextColor.GOLD));
        CommandSupport.info(source, "Party invite sent to " + target.getName() + ".");
    }

    private void accept(CommandSourceStack source, Player player) {
        if (!parties.accept(player.getUniqueId())) {
            CommandSupport.error(source, "You have no valid party invite.");
            return;
        }

        CommandSupport.info(source, "Party invite accepted.");
        notifyParty(player.getUniqueId(),
                Component.text(player.getName() + " joined the party.", NamedTextColor.GREEN));
    }

    private void decline(CommandSourceStack source, Player player) {
        CommandSupport.info(source,
                parties.decline(player.getUniqueId())
                        ? "Party invite declined."
                        : "You have no pending party invite.");
    }

    private void leave(CommandSourceStack source, Player player) {
        Party party = parties.find(player.getUniqueId()).orElse(null);
        if (party == null) {
            CommandSupport.error(source, "You are not in a party.");
            return;
        }

        UUID playerId = player.getUniqueId();
        boolean wasLeader = party.leader().equals(playerId);
        if (!parties.leave(playerId)) {
            CommandSupport.error(source, "Could not leave the party.");
            return;
        }

        CommandSupport.info(source, "You left your party.");
        if (!wasLeader) {
            notifyParty(party.leader(),
                    Component.text(player.getName() + " left the party.", NamedTextColor.YELLOW));
        }
    }

    private void disband(CommandSourceStack source, Player player) {
        Party party = parties.find(player.getUniqueId()).orElse(null);
        if (party == null || !party.leader().equals(player.getUniqueId())) {
            CommandSupport.error(source, "Only the party leader can disband the party.");
            return;
        }

        var members = party.members();
        if (!parties.disband(player.getUniqueId())) {
            CommandSupport.error(source, "Could not disband the party.");
            return;
        }

        Component message = Component.text("The party was disbanded.", NamedTextColor.RED);
        members.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(member -> member.sendMessage(message));
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

        notifyParty(target.getUniqueId(),
                Component.text(target.getName() + " is now the party leader.", NamedTextColor.GOLD));
    }

    private void chat(CommandSourceStack source, Player player, String[] args) {
        Party party = parties.find(player.getUniqueId()).orElse(null);
        if (party == null) {
            CommandSupport.error(source, "You are not in a party.");
            return;
        }

        if (args.length == 1) {
            boolean enabled = parties.togglePartyChat(player.getUniqueId());
            CommandSupport.info(source, "Party chat " + (enabled ? "enabled." : "disabled."));
            return;
        }

        String message = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        Component rendered = Component.text("[Party] ", NamedTextColor.AQUA)
                .append(Component.text(player.getName() + ": ", NamedTextColor.WHITE))
                .append(Component.text(message, NamedTextColor.WHITE));

        party.members().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(member -> member.sendMessage(rendered));
    }

    private void showParty(CommandSourceStack source, UUID self) {
        Party party = parties.find(self).orElse(null);
        if (party == null) {
            CommandSupport.info(source, "You are not in a party. Use /party create or accept an invite.");
            return;
        }

        source.getSender().sendMessage(Component.text(
                "Party (" + party.size() + "/" + Party.DEFAULT_MAX_SIZE + ")",
                NamedTextColor.GOLD));

        for (UUID memberId : party.members()) {
            Player online = Bukkit.getPlayer(memberId);
            String name = online == null ? memberId.toString() : online.getName();
            String marker = memberId.equals(party.leader()) ? " ★" : "";
            source.getSender().sendMessage(Component.text(" - " + name + marker, NamedTextColor.GRAY));
        }
    }

    private void notifyParty(UUID memberId, Component message) {
        parties.find(memberId).ifPresent(party -> party.members().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(member -> member.sendMessage(message)));
    }

    @Override
    public String permission() {
        return "rpg.parties";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1) {
            return List.of("create", "invite", "accept", "decline", "leave", "disband", "kick", "leader", "chat");
        }
        if (args.length == 2 && (args[0].equalsIgnoreCase("invite")
                || args[0].equalsIgnoreCase("kick")
                || args[0].equalsIgnoreCase("leader"))) {
            return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        }
        return List.of();
    }
}
