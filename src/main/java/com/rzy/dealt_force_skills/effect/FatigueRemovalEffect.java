package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class FatigueRemovalEffect extends AttributeStatusEffect {
    public FatigueRemovalEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xD6C85E,
                0.10D, "7670f35c-730d-4628-a026-c2364b4dcdf1",
                0.10D, "74e8e7bb-7c0f-4496-92bc-444e5eed62a4");
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % 20 == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && entity instanceof Player player) {
            player.getFoodData().setFoodLevel(Math.max(player.getFoodData().getFoodLevel(), 7));
        }
    }
}
