package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class AttributeStatusEffect extends MobEffect {
    public AttributeStatusEffect(MobEffectCategory category, int color,
                                 double movementSpeed, String movementUuid,
                                 double attackSpeed, String attackUuid) {
        super(category, color);
        if (movementSpeed != 0.0D) {
            addAttributeModifier(Attributes.MOVEMENT_SPEED, movementUuid, movementSpeed,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (attackSpeed != 0.0D) {
            addAttributeModifier(Attributes.ATTACK_SPEED, attackUuid, attackSpeed,
                    AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
    }
}
