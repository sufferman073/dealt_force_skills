package com.rzy.dealt_force_skills.boss;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class BossCombatRules {
    private BossCombatRules() {
    }

    public static boolean isSkillPlayerLike(Entity entity) {
        return entity instanceof Player || entity instanceof SkillPlayerLikeTarget;
    }

    public static boolean receivesNonPlayerSkillMultiplier(LivingEntity target) {
        return target != null && !isSkillPlayerLike(target);
    }

    public static boolean canInstantKill(LivingEntity target) {
        return target != null && !(target instanceof SkillPlayerLikeTarget);
    }

    public static boolean canStealFrom(LivingEntity target) {
        return target instanceof Player && !(target instanceof SkillPlayerLikeTarget);
    }
}
