package com.rzy.dealt_force_skills.character.manba;

import com.rzy.dealt_force_skills.config.DealtForceConfig;

public enum ManbaLens {
    PURPLE_LENS("紫透镜", 1.25D, 1.25D, 1.30D, 1.15D),
    YELLOW_LENS("黄透镜", 1.75D, 0.75D, 1.50D, 1.10D);

    private final String displayName;
    private final double rangeMultiplier;
    private final double widthMultiplier;
    private final double progressMultiplier;
    private final double blindMultiplier;

    ManbaLens(String displayName, double rangeMultiplier, double widthMultiplier,
              double progressMultiplier, double blindMultiplier) {
        String key = "characters.manba.loadout.lenses." + name().toLowerCase();
        this.displayName = displayName;
        this.rangeMultiplier = DealtForceConfig.doubleValue(key + ".range_multiplier", rangeMultiplier);
        this.widthMultiplier = DealtForceConfig.doubleValue(key + ".width_multiplier", widthMultiplier);
        this.progressMultiplier = DealtForceConfig.doubleValue(key + ".progress_multiplier", progressMultiplier);
        this.blindMultiplier = DealtForceConfig.doubleValue(key + ".blind_multiplier", blindMultiplier);
    }

    public String displayName() {
        return displayName;
    }

    public double rangeMultiplier() {
        return rangeMultiplier;
    }

    public double widthMultiplier() {
        return widthMultiplier;
    }

    public double progressMultiplier() {
        return progressMultiplier;
    }

    public double blindMultiplier() {
        return blindMultiplier;
    }
}
