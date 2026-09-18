package io.github.math0898.rpgframework.commands;

import io.github.math0898.rpgframework.items.ItemRegistry;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import java.util.Collection;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class GiveCommand implements BasicCommand {
    private final ItemRegistry items;

    public GiveCommand(ItemRegistry items) {
        this.items = items;
    }

    @Override
    public void execute(CommandSourceStack source, String[] args) {
        if (args.length < 1 || args.length > 2) {
            CommandSupport.error(source, "Usage: /rpg-give <item-id> [player]");
            return;
        }

        Player target;
        if (args.length == 2) {
            target = Bukkit.getPlayerExact(args[1]);
        } else {
            target = CommandSupport.requirePlayer(source);
        }
        if (target == null) {
            CommandSupport.error(source, "Target player is not online.");
            return;
        }

        var definition = items.find(args[0]).orElse(null);
        if (definition == null) {
            CommandSupport.error(source, "Unknown RPG item: " + args[0]);
            return;
        }

        var stack = definition.createItemStack();
        target.getInventory().addItem(stack).values()
                .forEach(leftover -> target.getWorld().dropItemNaturally(target.getLocation(), leftover));
        CommandSupport.info(source, "Gave " + definition.id() + " to " + target.getName() + ".");
    }

    @Override
    public String permission() {
        return "rpg.admin";
    }

    @Override
    public Collection<String> suggest(CommandSourceStack source, String[] args) {
        return args.length <= 1 ? items.ids() : Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
    }
}
