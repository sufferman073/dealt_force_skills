package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NikaidouCorrectionEffect extends MobEffect {
    public NikaidouCorrectionEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF0A34A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "13d04ad4-84f8-4862-a019-cb2af4dc9e82",
                0.35D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
