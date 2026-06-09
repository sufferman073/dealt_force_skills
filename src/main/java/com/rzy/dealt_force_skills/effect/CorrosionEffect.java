package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorrosionEffect extends MobEffect {
    public CorrosionEffect() {
        super(MobEffectCategory.NEUTRAL, 0xD9C25A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "a8b73998-c94c-47ab-9d40-fb8b9063ec74",
                -0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        return modifier.getAmount();
    }
}
