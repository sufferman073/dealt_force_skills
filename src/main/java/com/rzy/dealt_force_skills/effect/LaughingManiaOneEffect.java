package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LaughingManiaOneEffect extends MobEffect {
    public LaughingManiaOneEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF77C8);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "ad4ed317-937d-4bb9-a2f9-46066ce9290c",
                0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
