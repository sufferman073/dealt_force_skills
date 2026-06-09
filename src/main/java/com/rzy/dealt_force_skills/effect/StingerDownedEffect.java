package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StingerDownedEffect extends MobEffect {
    public StingerDownedEffect() {
        super(MobEffectCategory.NEUTRAL, 0x1D355C);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "ac0d7f52-2e0b-4e58-b1bf-d2bd7f4d6110",
                -0.9D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setSprinting(false);
        entity.hurtMarked = true;
    }
}
