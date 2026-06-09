package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SonicShockEffect extends MobEffect {
    public SonicShockEffect() {
        super(MobEffectCategory.NEUTRAL, 0xF2C84B);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "10d9565f-2099-4142-968c-38a8341bc8af",
                -0.3D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "4d0f4d63-4884-4231-8f2a-f90de61c2d73",
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
