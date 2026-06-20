package com.rzy.dealt_force_skills.character.manba;

import java.util.Set;

public record ManbaFlashlightStats(
        int maxDurabilityScaled,
        int regenPerTickScaled,
        int consumePerTickScaled,
        int progressPerTick,
        int decayPerTick,
        int blindTicks,
        double range,
        double halfAngleDegrees,
        boolean noBlindCost
) {
    private static final int SCALE = 100;
    private static final int BASE_DURABILITY = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.manba.manba_flashlight_stats.base_durability", 200 * SCALE);
    private static final double BASE_REGEN_PER_SECOND = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.manba.manba_flashlight_stats.base_regen_per_second", 2.0D);
    private static final double BASE_CONSUME_PER_SECOND = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.manba.manba_flashlight_stats.base_consume_per_second", 4.0D);
    private static final int BASE_PROGRESS_PER_TICK = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.manba.manba_flashlight_stats.base_progress_per_tick", 10);
    private static final int BASE_DECAY_PER_TICK = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.manba.manba_flashlight_stats.base_decay_per_tick", 5);
    private static final int BASE_BLIND_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.manba.manba_flashlight_stats.base_blind_ticks", 2 * 20);
    private static final double BASE_RANGE = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.manba.manba_flashlight_stats.base_range", 20.0D);
    private static final double BASE_HALF_ANGLE = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "characters.manba.flashlight.base_half_angle_degrees", 10.0D);

    public static ManbaFlashlightStats of(Set<ManbaBulb> bulbs, ManbaLens lens, Set<ManbaBattery> batteries) {
        double durabilityMultiplier = 1.0D;
        double regenMultiplier = 1.0D;
        double consumeMultiplier = 1.0D;
        double progressMultiplier = 1.0D;
        double blindMultiplier = 1.0D;
        boolean noBlindCost = false;

        for (ManbaBulb bulb : bulbs) {
            progressMultiplier *= bulb.progressMultiplier();
            blindMultiplier *= bulb.blindMultiplier();
            consumeMultiplier *= bulb.consumeMultiplier();
        }

        if (lens != null) {
            progressMultiplier *= lens.progressMultiplier();
            blindMultiplier *= lens.blindMultiplier();
        }

        for (ManbaBattery battery : batteries) {
            durabilityMultiplier *= battery.durabilityMultiplier();
            regenMultiplier *= battery.regenMultiplier();
            noBlindCost |= battery.noBlindCost();
        }

        int maxDurability = Math.max(SCALE, (int) Math.round(BASE_DURABILITY * durabilityMultiplier));
        int regen = Math.max(1, (int) Math.round(BASE_REGEN_PER_SECOND * regenMultiplier * SCALE / 20.0D));
        int consume = Math.max(1, (int) Math.round(BASE_CONSUME_PER_SECOND * consumeMultiplier * SCALE / 20.0D));
        int progress = Math.max(1, (int) Math.round(BASE_PROGRESS_PER_TICK * progressMultiplier));
        int blindTicks = Math.max(1, (int) Math.round(BASE_BLIND_TICKS * blindMultiplier));
        double range = BASE_RANGE * (lens == null ? 1.0D : lens.rangeMultiplier());
        double angle = Math.max(2.0D, BASE_HALF_ANGLE * (lens == null ? 1.0D : lens.widthMultiplier()));
        return new ManbaFlashlightStats(maxDurability, regen, consume, progress, BASE_DECAY_PER_TICK,
                blindTicks, range, angle, noBlindCost);
    }
}
