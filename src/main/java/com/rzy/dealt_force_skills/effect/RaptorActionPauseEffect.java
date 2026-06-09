package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class RaptorActionPauseEffect extends MobEffect {
    public RaptorActionPauseEffect() {
        super(MobEffectCategory.NEUTRAL, 0x87CEFA);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "9959f66d-b4e1-44ab-8d9d-dff2cf38b7d5",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "bd60f03c-ec9c-45fa-9282-3af8421a69f4",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setDeltaMovement(entity.getDeltaMovement().multiply(0.0D, 1.0D, 0.0D));
        entity.stopUsingItem();
        entity.hurtMarked = true;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
