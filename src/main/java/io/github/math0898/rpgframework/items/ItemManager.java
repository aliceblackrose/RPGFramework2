package io.github.math0898.rpgframework.items;

import java.io.File;
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
        registry = Objects.requireNonNull(itemRegistry);
    }

    public static void unbind() {
        registry = null;
    }

    public void awardItem(Player player, String name) {
        RpgItem item = requireRegistry().find(name).orElse(null);
        if (item == null) {
            return;
        }
        ItemStack stack = item.getItemStack();
        player.getInventory().addItem(stack).values()
                .forEach(leftover -> player.getWorld().dropItemNaturally(player.getLocation(), leftover));
    }

    public List<String> getItemNames() {
        return new ArrayList<>(requireRegistry().ids());
    }

    public ItemStack getItem(String name) {
        return requireRegistry().find(name).map(RpgItem::getItemStack).orElse(null);
    }

    public String findRpgItem(ItemStack item) {
        for (String id : requireRegistry().ids()) {
            ItemStack candidate = getItem(id);
            if (candidate != null && candidate.isSimilar(item)) {
                return id;
            }
        }
        return null;
    }

    public RpgItem getRpgItem(String name) {
        return requireRegistry().find(name).orElse(null);
    }

    public boolean hasItem(String name) {
        return requireRegistry().find(name).isPresent();
    }

    public void passives() {
        // Passive item effects are now expected to be event driven by the item that owns them.
    }

    public void parseFiles(File[] files) {
        requireRegistry().reload();
    }

    public int rateItem(ItemStack item) {
        String id = findRpgItem(item);
        RpgItem rpgItem = id == null ? null : getRpgItem(id);
        return rpgItem == null ? 0 : rpgItem.getGearScore();
    }

    public void replaceRecipies() {
        // Intentionally no-op; destructive vanilla recipe replacement was removed.
    }

    public static String increaseRarity(String value) {
        Objects.requireNonNull(value);
        return value;
    }

    public static String genName(char[] name) {
        Objects.requireNonNull(name);
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
