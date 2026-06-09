package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CatDadHissSlowEffect extends MobEffect {
    public CatDadHissSlowEffect() {
        super(MobEffectCategory.NEUTRAL, 0xD7B48A);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "f675fc3d-f07d-4bb0-a83d-c41374aee274",
                -0.20D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
