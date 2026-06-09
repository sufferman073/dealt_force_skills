package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LaughingManiaTwoEffect extends MobEffect {
    public LaughingManiaTwoEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE34D9B);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "d6db4fa0-762a-486d-838f-0fe364d65528",
                0.2D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
