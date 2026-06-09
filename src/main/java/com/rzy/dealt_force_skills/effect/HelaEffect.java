package com.rzy.dealt_force_skills.effect;

import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SuppressLocalHurtAnimation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class HelaEffect extends MobEffect {
    public HelaEffect() {
        super(MobEffectCategory.NEUTRAL, 0x7B3F98);
        addAttributeModifier(Attributes.MAX_HEALTH,
                "63e9d99f-b082-4616-914b-978be62ecaf2",
                -0.1D,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }
        if (entity.getHealth() <= 1.0F) {
            entity.hurt(entity.damageSources().genericKill(), Float.MAX_VALUE);
            return;
        }
        entity.setHealth(Math.max(0.01F, entity.getHealth() - 1.0F));
        if (entity instanceof ServerPlayer player) {
            NetworkHandler.sendToPlayer(new S2C_SuppressLocalHurtAnimation(3), player);
        }
    }
}
