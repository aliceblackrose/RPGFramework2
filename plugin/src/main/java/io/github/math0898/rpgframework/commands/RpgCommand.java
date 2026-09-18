package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.RPGFramework;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public final class RpgCommand implements BasicCommand {
    private final RPGFramework plugin;

    public RpgCommand(RPGFramework plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!source.getSender().hasPermission("rpg.admin")) {
                CommandSupport.error(source, "You do not have permission to reload RPGFramework.");
                return;
            }
            plugin.items().reload();
            CommandSupport.info(source, "RPGFramework item definitions reloaded.");
            return;
        }

        source.getSender().sendMessage(Component.text("RPGFramework ", NamedTextColor.GOLD)
                .append(Component.text(plugin.getPluginMeta().getVersion(), NamedTextColor.GRAY)));
        source.getSender().sendMessage(Component.text(
                "/classes, /party, /stats, /rpg-give; use /rpg reload as an admin.",
                NamedTextColor.GRAY));
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        if (args.length <= 1 && source.getSender().hasPermission("rpg.admin")) {
            return List.of("reload");
        }
        return List.of();
    }
}
