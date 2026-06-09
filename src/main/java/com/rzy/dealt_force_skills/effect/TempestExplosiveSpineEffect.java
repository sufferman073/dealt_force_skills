package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TempestExplosiveSpineEffect extends MobEffect {
    public TempestExplosiveSpineEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x7CFF5B);
        addAttributeModifier(Attributes.MOVEMENT_SPEED,
                "4029d79b-4a1a-4f19-9813-77d9e26f0a90",
                0.5D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
