package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.UUID;

public final class InjuryEffect extends MobEffect {
    public InjuryEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    public InjuryEffect(int color, Attribute attribute, UUID modifierId, double amount) {
        this(color);
        addAttributeModifier(attribute, modifierId.toString(), amount, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
