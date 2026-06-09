package com.rzy.dealt_force_skills.effect;

import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class StaminaBoostEffect extends AttributeStatusEffect {
    public StaminaBoostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x54C7EE,
                0.30D, "76063e53-ce19-4a67-b645-e025582535da",
                0.30D, "64658e9e-8b43-453c-9160-f4ec0342f58f");
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            ParcoolStaminaBridge.recoverLocalPercent(player, Math.max(1, amplifier + 1));
        }
    }
}
