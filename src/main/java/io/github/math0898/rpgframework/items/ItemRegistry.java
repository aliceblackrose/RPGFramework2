package io.github.math0898.rpgframework.items;

import io.github.math0898.rpgframework.RPGFramework;
import io.github.math0898.rpgframework.Rarity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public final class ItemRegistry {
    private static final List<String> BUNDLED_FILES = List.of(
            "items/krusk.yml",
            "items/other.yml",
            "items/eiryeras.yml",
            "items/feyrith.yml",
            "items/gods.yml",
            "items/vanilla.yml",
            "items/seignour.yml");

    private final RPGFramework plugin;
    private final Map<String, RpgItem> items = new LinkedHashMap<>();

    public ItemRegistry(RPGFramework plugin) {
        this.plugin = Objects.requireNonNull(plugin);
    }

    public synchronized void reload() {
        File itemDirectory = new File(plugin.getDataFolder(), "items");
        if (!itemDirectory.exists() && !itemDirectory.mkdirs()) {
            throw new IllegalStateException("Could not create " + itemDirectory);
        }

        for (String resource : BUNDLED_FILES) {
            File target = new File(plugin.getDataFolder(), resource);
            if (!target.isFile()) {
                plugin.saveResource(resource, false);
            }
        }

        Map<String, RpgItem> loaded = new LinkedHashMap<>();
        File[] files = itemDirectory.listFiles((directory, name) ->
                name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files != null) {
            for (File file : files) {
                parseFile(file, loaded);
            }
        }

        items.clear();
        items.putAll(loaded);
        plugin.getLogger().info("Loaded " + items.size() + " RPG items.");
    }

    public synchronized Optional<RpgItem> find(String id) {
        return Optional.ofNullable(items.get(normalizeId(id)));
    }

    public synchronized Collection<String> ids() {
        return List.copyOf(items.keySet());
    }

    private void parseFile(File file, Map<String, RpgItem> target) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String namespace = file.getName().replaceFirst("\\.ya?ml$", "").toLowerCase(Locale.ROOT);
        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }
            String id = namespace + ":" + compactKey(key);
            try {
                target.put(normalizeId(id), parseItem(id, section));
            } catch (RuntimeException exception) {
                plugin.log(Level.WARNING, "Skipping invalid RPG item " + id + " from " + file.getName(), exception);
            }
        }
    }

    private RpgItem parseItem(String id, ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "COBBLESTONE"));
        if (material == null) {
            throw new IllegalArgumentException("Unknown material");
        }

        String name = section.getString("name", id);
        Rarity rarity = enumValue(Rarity.class, section.getString("rarity"), Rarity.COMMON);
        EquipmentSlots slot = enumValue(EquipmentSlots.class, section.getString("slot"), EquipmentSlots.MATERIAL);
        ArmorTypes armorType = nullableEnum(ArmorTypes.class, section.getString("armor-type"));
        WeaponType weaponType = nullableEnum(WeaponType.class, section.getString("weapon-type"));
        List<String> description = List.copyOf(section.getStringList("description"));
        int health = section.getInt("stats.health", 0);
        int damage = section.getInt("stats.damage", 0);
        double armor = section.getDouble("stats.armor", 0.0);
        double toughness = section.getDouble("stats.toughness", 0.0);
        double attackSpeed = section.getDouble("stats.attack-speed", 0.0);

        return new RpgItem(
                id,
                rarity,
                slot,
                health,
                damage,
                armor,
                toughness,
                attackSpeed,
                armorType,
                weaponType,
                () -> createStack(
                        id, material, name, rarity, slot, description,
                        health, damage, armor, toughness, attackSpeed));
    }

    private ItemStack createStack(
            String id,
            Material material,
            String name,
            Rarity rarity,
            EquipmentSlots slot,
            List<String> description,
            int health,
            int damage,
            double armor,
            double toughness,
            double attackSpeed
    ) {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.displayName(Component.text(name, rarity.color()));

        List<Component> lore = new ArrayList<>();
        description.forEach(line -> lore.add(Component.text(line, NamedTextColor.GRAY)));
        if (health != 0 || damage != 0 || armor != 0 || toughness != 0 || attackSpeed != 0) {
            lore.add(Component.empty());
            if (damage != 0) lore.add(Component.text("Damage: " + damage, NamedTextColor.RED));
            if (attackSpeed != 0) lore.add(Component.text("Attack Speed: " + attackSpeed, NamedTextColor.AQUA));
            if (health != 0) lore.add(Component.text("Health: " + health, NamedTextColor.LIGHT_PURPLE));
            if (armor != 0) lore.add(Component.text("Armor: " + armor, NamedTextColor.GREEN));
            if (toughness != 0) lore.add(Component.text("Toughness: " + toughness, NamedTextColor.YELLOW));
        }
        meta.lore(lore);
        meta.setUnbreakable(true);

        addModifier(meta, id + "_health", Attribute.MAX_HEALTH, health / 5.0, slot);
        addModifier(meta, id + "_armor", Attribute.ARMOR, armor, slot);
        addModifier(meta, id + "_toughness", Attribute.ARMOR_TOUGHNESS, toughness, slot);
        addModifier(meta, id + "_attack_speed", Attribute.ATTACK_SPEED, attackSpeed, slot);
        addModifier(meta, id + "_attack_damage", Attribute.ATTACK_DAMAGE, damage / 5.0, slot);

        stack.setItemMeta(meta);
        return stack;
    }

    private void addModifier(
            ItemMeta meta,
            String keyPart,
            Attribute attribute,
            double amount,
            EquipmentSlots slot
    ) {
        if (amount == 0.0) {
            return;
        }
        var key = new org.bukkit.NamespacedKey(plugin, sanitizeKey(keyPart));
        var modifier = new AttributeModifier(
                key,
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                slot.group());
        meta.addAttributeModifier(attribute, modifier);
    }

    private static String sanitizeKey(String value) {
        return value.toLowerCase(Locale.ROOT).replace(':', '_').replaceAll("[^a-z0-9._/-]", "_");
    }

    private static String compactKey(String key) {
        StringBuilder result = new StringBuilder();
        boolean upper = true;
        for (char c : key.toCharArray()) {
            if (c == '-' || c == '_' || c == ' ') {
                upper = true;
            } else {
                result.append(upper ? Character.toUpperCase(c) : c);
                upper = false;
            }
        }
        return result.toString();
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> E enumValue(Class<E> type, String value, E fallback) {
        E parsed = nullableEnum(type, value);
        return parsed == null ? fallback : parsed;
    }

    private static <E extends Enum<E>> E nullableEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
