package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CatDadDownedEffect extends MobEffect {
    public CatDadDownedEffect() {
        super(MobEffectCategory.NEUTRAL, 0xB88758);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "02025f39-19e0-4ae0-9a3e-b321043fdac2",
                -0.85D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                "04aa6246-66cc-47d9-a5b6-d7e7cb949b18",
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
