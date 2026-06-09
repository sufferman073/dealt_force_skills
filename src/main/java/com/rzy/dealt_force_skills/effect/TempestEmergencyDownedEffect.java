package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TempestEmergencyDownedEffect extends MobEffect {
    public TempestEmergencyDownedEffect() {
        super(MobEffectCategory.NEUTRAL, 0xB060FF);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "9f233a89-2226-4c4f-9e22-f5bb6d862733",
                -0.85D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "56f88bc1-56d3-4ef2-8413-86fe034446c2",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.setSprinting(false);
        entity.stopUsingItem();
        entity.hurtMarked = true;
    }
}
