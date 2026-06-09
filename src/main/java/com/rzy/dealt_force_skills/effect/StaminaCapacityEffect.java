package com.rzy.dealt_force_skills.effect;

import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class StaminaCapacityEffect extends MobEffect {
    public StaminaCapacityEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x78B6FF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            ParcoolStaminaBridge.recoverLocalPercent(player, 5 * (amplifier + 1));
        }
    }
}
