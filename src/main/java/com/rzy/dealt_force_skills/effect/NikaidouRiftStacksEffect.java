package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class NikaidouRiftStacksEffect extends MobEffect {
    public NikaidouRiftStacksEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE04535);
        addAttributeModifier(Attributes.ATTACK_DAMAGE,
                "ef32e2f0-68e8-4bf1-a2f7-a3b4b582043c",
                0.04D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
