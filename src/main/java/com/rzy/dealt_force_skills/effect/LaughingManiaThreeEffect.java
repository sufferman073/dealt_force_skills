package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class LaughingManiaThreeEffect extends MobEffect {
    public LaughingManiaThreeEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xC81F71);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "f802962a-89ff-464c-ab50-0396ef0c51fc",
                -0.1D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
