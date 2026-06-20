package com.rzy.dealt_force_skills.skill;

import com.rzy.dealt_force_skills.registry.ModGameRules;
import net.minecraft.world.entity.player.Player;

public final class SkillCooldownHelper {
    private static final double REDUCTION_PER_LEVEL = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
            "experience_growth.general.cooldown_reduction_per_level", 0.001D);

    private SkillCooldownHelper() {
    }

    public static int ticks(Player player, int baseTicks) {
        if (baseTicks <= 0) {
            return 0;
        }
        int level = ModGameRules.effectiveExperienceLevel(player);
        double multiplier = Math.max(0.0D, 1.0D - level * REDUCTION_PER_LEVEL);
        return Math.max(1, (int) Math.ceil(baseTicks * multiplier));
    }

    public static long until(Player player, long now, int baseTicks) {
        return now + ticks(player, baseTicks);
    }
}
