package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class CatDadArmorReducedEffect extends MobEffect {
    public CatDadArmorReducedEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8C6B4B);
        addAttributeModifier(Attributes.ARMOR,
                "ad367c34-ce58-4694-a0f3-cdd519584044",
                -0.75D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                "c5388c63-58b6-4f4a-9a1b-03ab83d5f033",
                -0.75D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
