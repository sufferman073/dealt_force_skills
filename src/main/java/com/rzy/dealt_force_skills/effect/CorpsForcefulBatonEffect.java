package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorpsForcefulBatonEffect extends PlannedStatusEffect {
    public CorpsForcefulBatonEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xF5B34D);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "a8935092-2314-4afa-991a-aa02317f9084",
                6.00D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
