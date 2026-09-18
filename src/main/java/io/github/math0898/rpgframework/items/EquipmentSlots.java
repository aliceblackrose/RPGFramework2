package io.github.math0898.rpgframework.items;

import java.util.Locale;
import org.bukkit.inventory.EquipmentSlotGroup;

public enum EquipmentSlots {
    HEAD(EquipmentSlotGroup.HEAD, true),
    CHEST(EquipmentSlotGroup.CHEST, true),
    LEGS(EquipmentSlotGroup.LEGS, true),
    FEET(EquipmentSlotGroup.FEET, true),
    MAIN_HAND(EquipmentSlotGroup.MAINHAND, true),
    OFF_HAND(EquipmentSlotGroup.OFFHAND, true),
    MATERIAL(EquipmentSlotGroup.ANY, false);

    private final EquipmentSlotGroup group;
    private final boolean appliesAttributes;

    EquipmentSlots(EquipmentSlotGroup group, boolean appliesAttributes) {
        this.group = group;
        this.appliesAttributes = appliesAttributes;
    }

    public EquipmentSlotGroup group() {
        return group;
    }

    public boolean appliesAttributes() {
        return appliesAttributes;
    }

    public static EquipmentSlots fromConfig(String value) {
        if (value == null || value.isBlank()) {
            return MATERIAL;
        }

        String normalized = value.trim().toUpperCase(Locale.ROOT).replace('-', '_');
        return switch (normalized) {
            case "HAND", "MAINHAND", "MAIN_HAND" -> MAIN_HAND;
            case "ARTIFACT", "OFFHAND", "OFF_HAND" -> OFF_HAND;
            case "HEAD" -> HEAD;
            case "CHEST" -> CHEST;
            case "LEGS" -> LEGS;
            case "FEET" -> FEET;
            case "MATERIAL" -> MATERIAL;
            default -> throw new IllegalArgumentException("Unknown equipment slot: " + value);
        };
    }
}
