package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CorpsDecayedSoldierEffect extends PlannedStatusEffect {
    public CorpsDecayedSoldierEffect() {
        super(MobEffectCategory.HARMFUL, 0x5A6B45);
        addAttributeModifier(Attributes.MAX_HEALTH,
                "10fbe5de-34b6-49ef-b164-ec66c488f7b3",
                -0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "77df719f-a271-40ce-8142-96ac99e046c7",
                -0.25D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "07c8fb34-9e24-418e-9d16-ceb71cb9dded",
                -0.50D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
