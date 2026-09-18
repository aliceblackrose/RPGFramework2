package io.github.math0898.rpgframework.items;

public enum ArmorTypes {
    LIGHT,
    MEDIUM,
    HEAVY;

    public String getFormattedName() {
        return name().charAt(0) + name().substring(1).toLowerCase();
    }
}
