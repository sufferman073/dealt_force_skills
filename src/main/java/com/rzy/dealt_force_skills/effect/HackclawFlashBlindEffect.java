package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class HackclawFlashBlindEffect extends MobEffect {
    public HackclawFlashBlindEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFF2A6);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        applySuppression(entity);
    }

    public static void applySuppression(LivingEntity entity) {
        NoxFlashedEffect.suppressHostileTargeting(entity);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
