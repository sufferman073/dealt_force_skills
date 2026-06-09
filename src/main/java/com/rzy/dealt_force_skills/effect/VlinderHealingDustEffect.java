package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;

public class VlinderHealingDustEffect extends MobEffect {
    public VlinderHealingDustEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7DFFB2);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        for (MobEffectInstance effect : new ArrayList<>(entity.getActiveEffects())) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                entity.removeEffect(effect.getEffect());
            }
        }
        if (!entity.level().isClientSide && entity.tickCount % 18 == 0 && entity.getHealth() < entity.getMaxHealth()) {
            entity.heal(1.0F);
        }
    }
}
