package io.github.math0898.rpgframework.items;

import org.bukkit.inventory.EquipmentSlotGroup;

public enum EquipmentSlots {
    HEAD(EquipmentSlotGroup.HEAD),
    CHEST(EquipmentSlotGroup.CHEST),
    LEGS(EquipmentSlotGroup.LEGS),
    FEET(EquipmentSlotGroup.FEET),
    MAIN_HAND(EquipmentSlotGroup.MAINHAND),
    OFF_HAND(EquipmentSlotGroup.OFFHAND),
    MATERIAL(EquipmentSlotGroup.ANY);

    private final EquipmentSlotGroup group;

    EquipmentSlots(EquipmentSlotGroup group) {
        this.group = group;
    }

    public EquipmentSlotGroup group() {
        return group;
    }
}
