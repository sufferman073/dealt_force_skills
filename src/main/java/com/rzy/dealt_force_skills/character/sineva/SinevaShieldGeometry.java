package com.rzy.dealt_force_skills.character.sineva;

public final class SinevaShieldGeometry {
    public static final double SHIELD_WORLD_WIDTH = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.sineva.shield.world_width", 1.34D);
    public static final double SHIELD_WORLD_HEIGHT = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.sineva.shield.world_height", 2.22D);
    public static final double VIEWPORT_WORLD_WIDTH = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.sineva.shield.viewport_width", 0.74D);
    public static final double VIEWPORT_WORLD_HEIGHT = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.sineva.shield.viewport_height", 0.26D);
    public static final double VIEWPORT_FORWARD_OFFSET = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.sineva.shield.viewport_forward_offset", 0.64D);
    public static final double VIEWPORT_CENTER_EYE_OFFSET = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.sineva.shield.viewport_center_eye_offset", -0.03D);
    public static final double VIEWPORT_RAY_LENGTH = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.sineva_shield_geometry.viewport_ray_length", 50.0D);

    private SinevaShieldGeometry() {
    }
}
