package com.rzy.dealt_force_skills.character.manba;

import com.rzy.dealt_force_skills.config.DealtForceConfig;

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
        String key = "characters.manba.loadout.bulbs." + name().toLowerCase();
        this.displayName = displayName;
        this.progressMultiplier = DealtForceConfig.doubleValue(key + ".progress_multiplier", progressMultiplier);
        this.blindMultiplier = DealtForceConfig.doubleValue(key + ".blind_multiplier", blindMultiplier);
        this.consumeMultiplier = DealtForceConfig.doubleValue(key + ".consume_multiplier", consumeMultiplier);
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
