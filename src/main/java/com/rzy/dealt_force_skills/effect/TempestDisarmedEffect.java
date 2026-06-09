package com.rzy.dealt_force_skills.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class TempestDisarmedEffect extends MobEffect {
    public TempestDisarmedEffect() {
        super(MobEffectCategory.NEUTRAL, 0xFFB347);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        entity.stopUsingItem();
        if (entity instanceof Player player) {
            player.setSprinting(false);
            cooldown(player, player.getMainHandItem());
            cooldown(player, player.getOffhandItem());
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    private static void cooldown(Player player, ItemStack stack) {
        if (!stack.isEmpty()) {
            player.getCooldowns().addCooldown(stack.getItem(), 3);
        }
    }
}
