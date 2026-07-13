package com.rzy.dealt_force_skills.character.sineva;

public final class SinevaShieldGeometry {
    public static volatile double SHIELD_WORLD_WIDTH = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("SHIELD_WORLD_WIDTH", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.shield.world_width", 1.34));
    public static volatile double SHIELD_WORLD_HEIGHT = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("SHIELD_WORLD_HEIGHT", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.shield.world_height", 2.22));
    public static volatile double VIEWPORT_WORLD_WIDTH = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("VIEWPORT_WORLD_WIDTH", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.shield.viewport_width", 0.74));
    public static volatile double VIEWPORT_WORLD_HEIGHT = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("VIEWPORT_WORLD_HEIGHT", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.shield.viewport_height", 0.26));
    public static volatile double VIEWPORT_FORWARD_OFFSET = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("VIEWPORT_FORWARD_OFFSET", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.shield.viewport_forward_offset", 0.64));
    public static volatile double VIEWPORT_CENTER_EYE_OFFSET = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("VIEWPORT_CENTER_EYE_OFFSET", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.shield.viewport_center_eye_offset", -0.03));
    public static volatile double VIEWPORT_RAY_LENGTH = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("VIEWPORT_RAY_LENGTH", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.sineva.sineva_shield_geometry.viewport_ray_length", 50.0));
    private SinevaShieldGeometry() {
    }
}
