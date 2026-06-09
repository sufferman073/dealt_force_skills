package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class StingerStimSuppressionEffect extends MobEffect {
    public StingerStimSuppressionEffect() {
        super(MobEffectCategory.HARMFUL, 0x4F6B42);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "c816dcf1-3866-4ec0-94a6-876416cf9838",
                -0.2D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR,
                "a9b7fe74-f6ad-4c83-9e76-e54736a99883",
                -0.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
