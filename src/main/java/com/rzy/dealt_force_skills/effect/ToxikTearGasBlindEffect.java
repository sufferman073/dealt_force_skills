package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;

public class ToxikTearGasBlindEffect extends MobEffect {
    public ToxikTearGasBlindEffect() {
        super(MobEffectCategory.NEUTRAL, 0x48F0D8);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        suppressHostileTargeting(entity);
    }

    public static void suppressHostileTargeting(LivingEntity entity) {
        if (entity.level().isClientSide || !(entity instanceof Mob mob)) {
            return;
        }
        if (!(mob instanceof Enemy) && mob.getTarget() == null) {
            return;
        }
        mob.setTarget(null);
        mob.getNavigation().stop();
        mob.setLastHurtByMob(null);
        mob.setLastHurtByPlayer(null);
        mob.setLastHurtMob(null);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
