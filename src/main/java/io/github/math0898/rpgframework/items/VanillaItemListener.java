package io.github.math0898.rpgframework.items;

import java.util.Objects;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;

public final class VanillaItemListener implements Listener {
    private final ItemRegistry items;

    public VanillaItemListener(ItemRegistry items) {
        this.items = Objects.requireNonNull(items, "items");
    }

    @EventHandler
    public void onPrepareCraft(PrepareItemCraftEvent event) {
        ItemStack current = event.getInventory().getResult();
        if (current == null || current.getType().isAir()) {
            return;
        }

        items.vanillaReplacement(current.getType()).ifPresent(definition -> {
            ItemStack replacement = definition.createItemStack();
            replacement.setAmount(current.getAmount());
            event.getInventory().setResult(replacement);
        });
    }
}
