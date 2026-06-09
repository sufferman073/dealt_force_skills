package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ItemWeaknessEffect extends MobEffect {
    public ItemWeaknessEffect() {
        super(MobEffectCategory.HARMFUL, 0x6F6F75);
        addAttributeModifier(Attributes.MAX_HEALTH,
                "6db1fa18-d407-47ea-a5b3-4ac4b2d5f64c",
                -0.1D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
