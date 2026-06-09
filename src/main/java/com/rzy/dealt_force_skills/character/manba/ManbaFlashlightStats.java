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
    private static final int BASE_DURABILITY = 200 * SCALE;
    private static final double BASE_REGEN_PER_SECOND = 2.0D;
    private static final double BASE_CONSUME_PER_SECOND = 4.0D;
    private static final int BASE_PROGRESS_PER_TICK = 10;
    private static final int BASE_DECAY_PER_TICK = 5;
    private static final int BASE_BLIND_TICKS = 2 * 20;
    private static final double BASE_RANGE = 20.0D;
    private static final double BASE_HALF_ANGLE = 10.0D;

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
