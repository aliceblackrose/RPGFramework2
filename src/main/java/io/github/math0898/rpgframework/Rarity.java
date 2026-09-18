package io.github.math0898.rpgframework;

import net.kyori.adventure.text.format.TextColor;

public enum Rarity {
    COMMON(0xDDDDDD),
    UNCOMMON(0x53C975),
    RARE(0x23A5DB),
    LEGENDARY(0xF3B36B),
    HEROIC(0xF454DA),
    MYTHIC(0xD93747),
    RELIC(0xFFFDD0);

    private final int rgb;

    Rarity(int rgb) {
        this.rgb = rgb;
    }

    public TextColor color() {
        return TextColor.color(rgb);
    }

    public String getHexColor() {
        return "#%06X".formatted(rgb);
    }
}
