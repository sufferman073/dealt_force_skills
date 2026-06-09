package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NikaidouCoreEffect extends MobEffect {
    public NikaidouCoreEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFFE79A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "fc4dc11f-d19b-4c77-b13f-032b045af856",
                0.07D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
