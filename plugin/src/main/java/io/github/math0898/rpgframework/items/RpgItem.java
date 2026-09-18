package io.github.math0898.rpgframework.items;

import io.github.math0898.rpgframework.Rarity;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.inventory.ItemStack;

public final class RpgItem {
    private final String id;
    private final Rarity rarity;
    private final EquipmentSlots slot;
    private final int health;
    private final int damage;
    private final double armor;
    private final double toughness;
    private final double attackSpeed;
    private final ArmorTypes armorType;
    private final WeaponType weaponType;
    private final Supplier<ItemStack> factory;

    RpgItem(
            String id,
            Rarity rarity,
            EquipmentSlots slot,
            int health,
            int damage,
            double armor,
            double toughness,
            double attackSpeed,
            ArmorTypes armorType,
            WeaponType weaponType,
            Supplier<ItemStack> factory
    ) {
        this.id = Objects.requireNonNull(id);
        this.rarity = Objects.requireNonNull(rarity);
        this.slot = Objects.requireNonNull(slot);
        this.health = health;
        this.damage = damage;
        this.armor = armor;
        this.toughness = toughness;
        this.attackSpeed = attackSpeed;
        this.armorType = armorType;
        this.weaponType = weaponType;
        this.factory = Objects.requireNonNull(factory);
    }

    public String id() {
        return id;
    }

    public Rarity rarity() {
        return rarity;
    }

    public EquipmentSlots getSlot() {
        return slot;
    }

    public int getHealth() {
        return health;
    }

    public int getDamage() {
        return damage;
    }

    public double getArmor() {
        return armor;
    }

    public double getToughness() {
        return toughness;
    }

    public double getAttackSpeed() {
        return attackSpeed;
    }

    public ArmorTypes getArmorType() {
        return armorType;
    }

    public WeaponType getWeaponType() {
        return weaponType;
    }

    public int getGearScore() {
        return (int) (health + (damage * 5.0) + (armor * 2.0) + (rarity.ordinal() * 10.0));
    }

    public ItemStack getItemStack() {
        return factory.get();
    }
}
