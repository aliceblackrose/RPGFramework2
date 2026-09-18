package io.github.math0898.rpgframework.items;

public enum WeaponType {
    SWORD,
    AXE,
    BOW,
    CROSSBOW,
    STAFF,
    DAGGER,
    MACE,
    SPEAR,
    OTHER;

    public String getFormattedName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
