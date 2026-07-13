package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorpsWarmEnforcementEffect extends PlannedStatusEffect {
    public CorpsWarmEnforcementEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF08A3C);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "69f8c2a2-cd07-4e22-9b6d-74797537f401",
                0.50D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
