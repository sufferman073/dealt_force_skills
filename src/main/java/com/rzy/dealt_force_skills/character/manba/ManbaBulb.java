package com.rzy.dealt_force_skills.character.manba;

public enum ManbaBulb {
    RED_BULB("红灯泡", 1.50D, 1.25D, 1.12D),
    GREEN_BULB("绿灯泡", 1.40D, 1.10D, 1.0D),
    WHITE_BULB("白灯泡", 1.25D, 1.25D, 1.0D),
    YELLOW_BULB("黄灯泡", 1.10D, 1.40D, 1.0D),
    PURPLE_BULB("紫灯泡", 1.0D, 1.0D, 0.64D);

    private final String displayName;
    private final double progressMultiplier;
    private final double blindMultiplier;
    private final double consumeMultiplier;

    ManbaBulb(String displayName, double progressMultiplier, double blindMultiplier, double consumeMultiplier) {
        this.displayName = displayName;
        this.progressMultiplier = progressMultiplier;
        this.blindMultiplier = blindMultiplier;
        this.consumeMultiplier = consumeMultiplier;
    }

    public String displayName() {
        return displayName;
    }

    public double progressMultiplier() {
        return progressMultiplier;
    }

    public double blindMultiplier() {
        return blindMultiplier;
    }

    public double consumeMultiplier() {
        return consumeMultiplier;
    }
}
