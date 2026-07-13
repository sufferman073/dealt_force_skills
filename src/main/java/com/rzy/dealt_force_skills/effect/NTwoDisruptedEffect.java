package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NTwoDisruptedEffect extends MobEffect {
    public NTwoDisruptedEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8FDFFF);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "78e3f7e5-cb3f-4779-b698-50e0ddfd761f",
                -0.45D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
