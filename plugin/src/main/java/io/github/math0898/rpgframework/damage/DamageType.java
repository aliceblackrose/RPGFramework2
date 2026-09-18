package io.github.math0898.rpgframework.damage;

public enum DamageType {
    UNSPECIFIED(DamageArchetype.PHYSICAL),
    SLASH(DamageArchetype.PHYSICAL),
    PUNCTURE(DamageArchetype.PHYSICAL),
    IMPACT(DamageArchetype.PHYSICAL),
    FIRE(DamageArchetype.MAGIC),
    AIR(DamageArchetype.MAGIC),
    WATER(DamageArchetype.MAGIC),
    EARTH(DamageArchetype.MAGIC),
    ELECTRIC(DamageArchetype.MAGIC),
    NATURE(DamageArchetype.MAGIC),
    ICE(DamageArchetype.MAGIC),
    ABYSS(DamageArchetype.MAGIC),
    ENDER(DamageArchetype.MAGIC),
    VOID(DamageArchetype.MAGIC),
    HOLY(DamageArchetype.MAGIC);

    private final DamageArchetype archetype;

    DamageType(DamageArchetype archetype) {
        this.archetype = archetype;
    }

    public DamageArchetype archetype() {
        return archetype;
    }

    public boolean isPhysical() {
        return archetype == DamageArchetype.PHYSICAL;
    }

    public static String archetype(DamageType type) {
        return type.archetype.name();
    }
}
