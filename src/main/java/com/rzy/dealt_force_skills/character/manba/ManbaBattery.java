package com.rzy.dealt_force_skills.character.manba;

import com.rzy.dealt_force_skills.config.DealtForceConfig;

public enum ManbaBattery {
    GREEN_BATTERY("绿电池", 2.0D, 0.5D, false),
    YELLOW_BATTERY("黄电池", 1.5D, 1.0D, false),
    WHITE_BATTERY("白电池", 1.0D, 1.0D, true);

    private final String displayName;
    private final double durabilityMultiplier;
    private final double regenMultiplier;
    private final boolean noBlindCost;

    ManbaBattery(String displayName, double durabilityMultiplier, double regenMultiplier, boolean noBlindCost) {
        String key = "characters.manba.loadout.batteries." + name().toLowerCase();
        this.displayName = displayName;
        this.durabilityMultiplier = DealtForceConfig.doubleValue(key + ".durability_multiplier", durabilityMultiplier);
        this.regenMultiplier = DealtForceConfig.doubleValue(key + ".regen_multiplier", regenMultiplier);
        this.noBlindCost = DealtForceConfig.booleanValue(key + ".no_blind_cost", noBlindCost);
    }

    public String displayName() {
        return displayName;
    }

    public double durabilityMultiplier() {
        return durabilityMultiplier;
    }

    public double regenMultiplier() {
        return regenMultiplier;
    }

    public boolean noBlindCost() {
        return noBlindCost;
    }
}
