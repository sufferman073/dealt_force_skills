package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ToxikAdrenalineEffect extends MobEffect {
    private static final String ATTACK_SPEED_UUID = "a35d6e0e-230b-4875-99db-36c6d2e772ed";
    private static final double PER_LEVEL_MULTIPLIER = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("effects.toxikadrenalineeffect.per_level_multiplier", 0.2D);

    public ToxikAdrenalineEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xFF4D4D);
        addAttributeModifier(Attributes.ATTACK_SPEED,
                ATTACK_SPEED_UUID,
                PER_LEVEL_MULTIPLIER,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
        if (ATTACK_SPEED_UUID.equals(modifier.getId().toString())) {
            return PER_LEVEL_MULTIPLIER * (amplifier + 1);
        }
        return super.getAttributeModifierValue(amplifier, modifier);
    }
}
