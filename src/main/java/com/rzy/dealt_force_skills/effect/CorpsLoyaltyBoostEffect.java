package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorpsLoyaltyBoostEffect extends PlannedStatusEffect {
    public CorpsLoyaltyBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE46A22);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "2f8256b6-aa5c-4792-aa66-e2c8328006be",
                0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "f7cedd7c-7815-4c39-8e57-66b8fd8a0b8f",
                0.05D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
