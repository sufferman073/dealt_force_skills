package com.rzy.dealt_force_skills.character.gambler;

public enum GamblerRarity {
    RED(0xFFFF5656),
    GOLD(0xFFFFD35A),
    PURPLE(0xFFB78CFF);

    private final int color;

    GamblerRarity(int color) {
        this.color = color;
    }

    public int color() {
        return color;
    }
}
