package io.github.math0898.rpgframework.items;

import io.github.math0898.rpgframework.RPGFramework;
import io.github.math0898.rpgframework.Rarity;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.regex.Pattern;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class ItemRegistry {
    private static final List<String> BUNDLED_FILES = List.of(
            "items/krusk.yml",
            "items/other.yml",
            "items/eiryeras.yml",
            "items/feyrith.yml",
            "items/gods.yml",
            "items/vanilla.yml",
            "items/seignour.yml");
    private static final Pattern HEX = Pattern.compile("[0-9a-fA-F]{6}");

    private final RPGFramework plugin;
    private final NamespacedKey itemIdKey;
    private final Map<String, RpgItem> items = new LinkedHashMap<>();
    private final Map<Material, RpgItem> vanillaByMaterial = new LinkedHashMap<>();

    public ItemRegistry(RPGFramework plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.itemIdKey = new NamespacedKey(plugin, "item_id");
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
            java.util.Arrays.sort(files, Comparator.comparing(File::getName));
            for (File file : files) {
                parseFile(file, loaded);
            }
        }

        Map<Material, RpgItem> vanilla = new LinkedHashMap<>();
        for (RpgItem item : loaded.values()) {
            if (item.id().startsWith("vanilla:")) {
                vanilla.put(item.material(), item);
            }
        }

        items.clear();
        items.putAll(loaded);
        vanillaByMaterial.clear();
        vanillaByMaterial.putAll(vanilla);

        plugin.getLogger().info("Loaded " + items.size() + " RPG items.");
    }

    public synchronized Optional<RpgItem> find(String id) {
        return Optional.ofNullable(items.get(normalizeId(id)));
    }

    public synchronized Collection<RpgItem> all() {
        return List.copyOf(items.values());
    }

    public synchronized Collection<String> ids() {
        return List.copyOf(items.keySet());
    }

    public Optional<RpgItem> identify(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return Optional.empty();
        }
        ItemMeta meta = stack.getItemMeta();
        String id = meta.getPersistentDataContainer().get(itemIdKey, PersistentDataType.STRING);
        return id == null ? Optional.empty() : find(id);
    }

    public synchronized Optional<RpgItem> vanillaReplacement(Material material) {
        return Optional.ofNullable(vanillaByMaterial.get(Objects.requireNonNull(material, "material")));
    }

    private void parseFile(File file, Map<String, RpgItem> target) {
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(file);
        String namespace = file.getName().replaceFirst("\\.ya?ml$", "").toLowerCase(Locale.ROOT);

        for (String key : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(key);
            if (section == null) {
                continue;
            }

            String id = normalizeId(namespace + ":" + compactKey(key));
            try {
                RpgItem item = parseItem(id, section);
                RpgItem duplicate = target.putIfAbsent(id, item);
                if (duplicate != null) {
                    throw new IllegalStateException("Duplicate RPG item id: " + id);
                }
            } catch (RuntimeException exception) {
                plugin.log(Level.WARNING, "Skipping invalid RPG item " + id + " from " + file.getName(), exception);
            }
        }
    }

    private RpgItem parseItem(String id, ConfigurationSection section) {
        Material material = Material.matchMaterial(section.getString("material", "COBBLESTONE"));
        if (material == null || material.isAir()) {
            throw new IllegalArgumentException("Unknown or invalid material");
        }

        String name = section.getString("name", id);
        Rarity rarity = requiredEnum(Rarity.class, section.getString("rarity", "COMMON"));
        EquipmentSlots slot = EquipmentSlots.fromConfig(section.getString("slot", "MATERIAL"));
        ArmorTypes armorType = optionalEnum(ArmorTypes.class, section.getString("armor-type"));
        WeaponType weaponType = optionalEnum(WeaponType.class, section.getString("weapon-type"));
        List<String> description = List.copyOf(section.getStringList("description"));
        int health = section.getInt("stats.health", 0);
        int damage = section.getInt("stats.damage", 0);
        double armor = section.getDouble("stats.armor", 0.0);
        double toughness = section.getDouble("stats.toughness", 0.0);
        double attackSpeed = section.getDouble("stats.attack-speed", 0.0);

        return new RpgItem(
                id,
                name,
                material,
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
                        id,
                        material,
                        name,
                        rarity,
                        slot,
                        description,
                        health,
                        damage,
                        armor,
                        toughness,
                        attackSpeed));
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

        meta.displayName(renderText(name, rarity.color()));

        List<Component> lore = new ArrayList<>();
        description.forEach(line -> lore.add(renderText(line, NamedTextColor.GRAY)));
        if (health != 0 || damage != 0 || armor != 0.0 || toughness != 0.0 || attackSpeed != 0.0) {
            lore.add(Component.empty());
            if (damage != 0) {
                lore.add(Component.text("Damage: " + damage, NamedTextColor.RED));
            }
            if (attackSpeed != 0.0) {
                lore.add(Component.text("Attack Speed: " + attackSpeed, NamedTextColor.AQUA));
            }
            if (health != 0) {
                lore.add(Component.text("Health: " + health, NamedTextColor.LIGHT_PURPLE));
            }
            if (armor != 0.0) {
                lore.add(Component.text("Armor: " + armor, NamedTextColor.GREEN));
            }
            if (toughness != 0.0) {
                lore.add(Component.text("Toughness: " + toughness, NamedTextColor.YELLOW));
            }
        }

        meta.lore(lore);
        meta.setUnbreakable(true);
        meta.getPersistentDataContainer().set(itemIdKey, PersistentDataType.STRING, normalizeId(id));

        if (slot.appliesAttributes()) {
            addModifier(meta, id + "_health", Attribute.MAX_HEALTH, health / 5.0, slot);
            addModifier(meta, id + "_armor", Attribute.ARMOR, armor, slot);
            addModifier(meta, id + "_toughness", Attribute.ARMOR_TOUGHNESS, toughness, slot);
            addModifier(meta, id + "_attack_speed", Attribute.ATTACK_SPEED, attackSpeed, slot);
            addModifier(meta, id + "_attack_damage", Attribute.ATTACK_DAMAGE, damage / 5.0, slot);
        }

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

        var key = new NamespacedKey(plugin, sanitizeKey(keyPart));
        var modifier = new AttributeModifier(
                key,
                amount,
                AttributeModifier.Operation.ADD_NUMBER,
                slot.group());
        meta.addAttributeModifier(attribute, modifier);
    }

    private static Component renderText(String input, TextColor defaultColor) {
        if (input == null || input.isEmpty()) {
            return Component.empty();
        }

        TextComponent.Builder result = Component.text();
        StringBuilder buffer = new StringBuilder();
        TextColor color = defaultColor;
        boolean underlined = false;
        boolean bold = false;
        boolean italic = false;

        for (int index = 0; index < input.length();) {
            if (input.charAt(index) == '#'
                    && index + 7 <= input.length()
                    && HEX.matcher(input.substring(index + 1, index + 7)).matches()) {
                appendSegment(result, buffer, color, underlined, bold, italic);
                color = TextColor.color(Integer.parseInt(input.substring(index + 1, index + 7), 16));
                index += 7;
                continue;
            }

            if (input.charAt(index) == '§' && index + 1 < input.length()) {
                char code = Character.toLowerCase(input.charAt(index + 1));
                if (code == 'n' || code == 'l' || code == 'o' || code == 'r') {
                    appendSegment(result, buffer, color, underlined, bold, italic);
                    switch (code) {
                        case 'n' -> underlined = true;
                        case 'l' -> bold = true;
                        case 'o' -> italic = true;
                        case 'r' -> {
                            color = defaultColor;
                            underlined = false;
                            bold = false;
                            italic = false;
                        }
                        default -> throw new IllegalStateException("Unexpected formatting code");
                    }
                    index += 2;
                    continue;
                }
            }

            buffer.append(input.charAt(index));
            index++;
        }

        appendSegment(result, buffer, color, underlined, bold, italic);
        return result.build();
    }

    private static void appendSegment(
            TextComponent.Builder result,
            StringBuilder buffer,
            TextColor color,
            boolean underlined,
            boolean bold,
            boolean italic
    ) {
        if (buffer.isEmpty()) {
            return;
        }

        Component component = Component.text(buffer.toString(), color);
        if (underlined) {
            component = component.decorate(TextDecoration.UNDERLINED);
        }
        if (bold) {
            component = component.decorate(TextDecoration.BOLD);
        }
        if (italic) {
            component = component.decorate(TextDecoration.ITALIC);
        }

        result.append(component);
        buffer.setLength(0);
    }

    private static String sanitizeKey(String value) {
        return value.toLowerCase(Locale.ROOT)
                .replace(':', '_')
                .replaceAll("[^a-z0-9._/-]", "_");
    }

    private static String compactKey(String key) {
        StringBuilder result = new StringBuilder();
        boolean upper = true;
        for (char character : key.toCharArray()) {
            if (character == '-' || character == '_' || character == ' ') {
                upper = true;
            } else {
                result.append(upper ? Character.toUpperCase(character) : character);
                upper = false;
            }
        }
        return result.toString();
    }

    private static String normalizeId(String id) {
        return id == null ? "" : id.trim().toLowerCase(Locale.ROOT);
    }

    private static <E extends Enum<E>> E requiredEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(type.getSimpleName() + " must not be blank");
        }
        try {
            return Enum.valueOf(type, normalizeEnum(value));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown " + type.getSimpleName() + ": " + value, exception);
        }
    }

    private static <E extends Enum<E>> E optionalEnum(Class<E> type, String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return requiredEnum(type, value);
    }

    private static String normalizeEnum(String value) {
        return value.trim().toUpperCase(Locale.ROOT).replace('-', '_').replace(' ', '_');
    }
}
