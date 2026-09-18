package io.github.math0898.rpgframework.items;

import io.github.math0898.rpgframework.Rarity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Deprecated(forRemoval = false)
public final class ItemManager {
    private static final ItemManager INSTANCE = new ItemManager();
    private static ItemRegistry registry;

    private ItemManager() {
    }

    public static ItemManager getInstance() {
        return INSTANCE;
    }

    public static void bind(ItemRegistry itemRegistry) {
        registry = Objects.requireNonNull(itemRegistry, "itemRegistry");
    }

    public static void unbind() {
        registry = null;
    }

    public void awardItem(Player player, String name) {
        Objects.requireNonNull(player, "player");
        RpgItem item = requireRegistry().find(name).orElse(null);
        if (item == null) {
            return;
        }

        ItemStack stack = item.createItemStack();
        player.getInventory().addItem(stack).values()
                .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }

    public List<String> getItemNames() {
        return new ArrayList<>(requireRegistry().ids());
    }

    public ItemStack getItem(String name) {
        return requireRegistry().find(name).map(RpgItem::createItemStack).orElse(null);
    }

    public String findRpgItem(ItemStack item) {
        return requireRegistry().identify(item).map(RpgItem::id).orElse(null);
    }

    public RpgItem getRpgItem(String name) {
        return requireRegistry().find(name).orElse(null);
    }

    public boolean hasItem(String name) {
        return requireRegistry().find(name).isPresent();
    }

    public int rateItem(ItemStack item) {
        return requireRegistry().identify(item).map(RpgItem::gearScore).orElse(0);
    }

    public static String increaseRarity(String value) {
        Objects.requireNonNull(value, "value");
        Rarity rarity = Rarity.valueOf(value.trim().toUpperCase(Locale.ROOT));
        Rarity[] values = Rarity.values();
        int next = Math.min(values.length - 1, rarity.ordinal() + 1);
        return values[next].name();
    }

    public static String genName(char[] name) {
        Objects.requireNonNull(name, "name");
        StringBuilder result = new StringBuilder(name.length);
        boolean uppercase = true;
        for (char character : name) {
            if (character == '_') {
                result.append(' ');
                uppercase = true;
            } else {
                result.append(uppercase ? Character.toUpperCase(character) : Character.toLowerCase(character));
                uppercase = false;
            }
        }
        return result.toString();
    }

    private static ItemRegistry requireRegistry() {
        return Objects.requireNonNull(registry, "RPGFramework is not enabled");
    }
}
