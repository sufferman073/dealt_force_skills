package com.rzy.dealt_force_skills.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public final class ReconRevealThrottle {
    private static final String REVEAL_UNTIL = "dealt_force_skills.recon_reveal_until";

    private ReconRevealThrottle() {
    }

    public static boolean tryStart(LivingEntity target, int ticks) {
        if (target == null || target.level().isClientSide || ticks <= 0) {
            return false;
        }
        long now = target.level().getGameTime();
        CompoundTag tag = target.getPersistentData();
        long until = tag.getLong(REVEAL_UNTIL);
        if (until > now) {
            return false;
        }
        tag.putLong(REVEAL_UNTIL, now + ticks);
        return true;
    }
}
