package com.rzy.dealt_force_skills.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class RandomEffectConsumableItem extends DfsUseItem {
    private final List<List<EffectConsumableItem.EffectEntry>> outcomes;

    public RandomEffectConsumableItem(Properties properties,
                                      DfsItemQuality quality,
                                      String tooltipKey,
                                      int useTicks,
                                      Supplier<SoundEvent> startSound,
                                      Supplier<SoundEvent> finishSound,
                                      String startMessageKey,
                                      String finishMessageKey,
                                      List<List<EffectConsumableItem.EffectEntry>> outcomes) {
        super(properties, quality, tooltipKey, useTicks, startSound, finishSound, startMessageKey, finishMessageKey);
        this.outcomes = outcomes;
    }

    @Override
    protected boolean applyUseEffect(ItemStack stack, Level level, ServerPlayer player) {
        if (outcomes.isEmpty()) {
            return false;
        }
        List<EffectConsumableItem.EffectEntry> selected = outcomes.get(player.getRandom().nextInt(outcomes.size()));
        for (EffectConsumableItem.EffectEntry entry : selected) {
            MobEffect effect = entry.effect().get();
            if (effect == null) {
                continue;
            }
            player.addEffect(new MobEffectInstance(effect, entry.durationTicks(), entry.amplifier(),
                    false, true, true), player);
        }
        return true;
    }
}
