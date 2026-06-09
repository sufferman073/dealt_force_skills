package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class VlinderMedicalWasteInterferenceEffect extends MobEffect {
    public VlinderMedicalWasteInterferenceEffect() {
        super(MobEffectCategory.NEUTRAL, 0x8B6BFF);
        addAttributeModifier(Attributes.ARMOR,
                "1374a8e4-cf19-4f31-aa16-73f36ea0f2b8",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
        addAttributeModifier(Attributes.ARMOR_TOUGHNESS,
                "3688e45b-14f2-4c9d-a20b-a616f80db98f",
                -1.0D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
}
