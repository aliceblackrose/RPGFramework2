package io.github.math0898.rpgframework.items;

import io.github.math0898.rpgframework.Rarity;
import java.util.Objects;
import java.util.function.Supplier;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public final class RpgItem {
    private final String id;
    private final String displayName;
    private final Material material;
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
            String displayName,
            Material material,
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
        this.id = Objects.requireNonNull(id, "id");
        this.displayName = Objects.requireNonNull(displayName, "displayName");
        this.material = Objects.requireNonNull(material, "material");
        this.rarity = Objects.requireNonNull(rarity, "rarity");
        this.slot = Objects.requireNonNull(slot, "slot");
        this.health = health;
        this.damage = damage;
        this.armor = armor;
        this.toughness = toughness;
        this.attackSpeed = attackSpeed;
        this.armorType = armorType;
        this.weaponType = weaponType;
        this.factory = Objects.requireNonNull(factory, "factory");
    }

    public String id() {
        return id;
    }

    public String displayName() {
        return displayName;
    }

    public Material material() {
        return material;
    }

    public Rarity rarity() {
        return rarity;
    }

    public EquipmentSlots slot() {
        return slot;
    }

    public int health() {
        return health;
    }

    public int damage() {
        return damage;
    }

    public double armor() {
        return armor;
    }

    public double toughness() {
        return toughness;
    }

    public double attackSpeed() {
        return attackSpeed;
    }

    public ArmorTypes armorType() {
        return armorType;
    }

    public WeaponType weaponType() {
        return weaponType;
    }

    public int gearScore() {
        double score = Math.max(0.0, health)
                + (Math.max(0.0, damage) * 5.0)
                + (Math.max(0.0, armor) * 2.0)
                + Math.max(0.0, toughness)
                + (rarity.ordinal() * 10.0);
        return (int) Math.min(Integer.MAX_VALUE, Math.round(score));
    }

    public ItemStack createItemStack() {
        return factory.get();
    }

    @Deprecated(forRemoval = false)
    public EquipmentSlots getSlot() {
        return slot();
    }

    @Deprecated(forRemoval = false)
    public int getHealth() {
        return health();
    }

    @Deprecated(forRemoval = false)
    public int getDamage() {
        return damage();
    }

    @Deprecated(forRemoval = false)
    public double getArmor() {
        return armor();
    }

    @Deprecated(forRemoval = false)
    public double getToughness() {
        return toughness();
    }

    @Deprecated(forRemoval = false)
    public double getAttackSpeed() {
        return attackSpeed();
    }

    @Deprecated(forRemoval = false)
    public ArmorTypes getArmorType() {
        return armorType();
    }

    @Deprecated(forRemoval = false)
    public WeaponType getWeaponType() {
        return weaponType();
    }

    @Deprecated(forRemoval = false)
    public int getGearScore() {
        return gearScore();
    }

    @Deprecated(forRemoval = false)
    public ItemStack getItemStack() {
        return createItemStack();
    }
}
