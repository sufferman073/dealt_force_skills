package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class VlinderVitalDownedEffect extends MobEffect {
    public VlinderVitalDownedEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFF77B7);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "f597d39a-fd8e-44c5-b88f-b7616120786d",
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
