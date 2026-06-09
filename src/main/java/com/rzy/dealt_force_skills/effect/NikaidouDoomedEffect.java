package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NikaidouDoomedEffect extends MobEffect {
    public NikaidouDoomedEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFF2A22);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "fb5b57d6-4259-435b-b5fd-448e756db771",
                0.50D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.clearFire();
        entity.hurtMarked = true;
    }
}
