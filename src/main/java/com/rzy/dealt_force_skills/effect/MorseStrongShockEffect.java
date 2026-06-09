package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class MorseStrongShockEffect extends MobEffect {
    public MorseStrongShockEffect() {
        super(MobEffectCategory.NEUTRAL, 0xF2C84B);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "e0c287bf-fd0a-4af7-92a2-f0b19d3c26f8",
                -0.3D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "f5a0b2db-577c-4fe1-8c63-e7fd15878464",
                -0.3D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        double factor = amplifier >= 1 ? 0.20D : 0.55D;
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(factor, 1.0D, factor));
        entity.hurtMarked = true;
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return amplifier >= 1 ? -0.75D : -0.30D;
    }
}
