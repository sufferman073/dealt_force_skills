package com.rzy.dealt_force_skills.character.manba;

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
        this.displayName = displayName;
        this.rangeMultiplier = rangeMultiplier;
        this.widthMultiplier = widthMultiplier;
        this.progressMultiplier = progressMultiplier;
        this.blindMultiplier = blindMultiplier;
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
