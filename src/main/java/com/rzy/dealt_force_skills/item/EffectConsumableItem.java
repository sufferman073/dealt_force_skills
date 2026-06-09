package com.rzy.dealt_force_skills.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class EffectConsumableItem extends DfsUseItem {
    private final List<EffectEntry> effects;

    public EffectConsumableItem(Properties properties,
                                DfsItemQuality quality,
                                String tooltipKey,
                                int useTicks,
                                Supplier<SoundEvent> startSound,
                                Supplier<SoundEvent> finishSound,
                                String startMessageKey,
                                String finishMessageKey,
                                List<EffectEntry> effects) {
        super(properties, quality, tooltipKey, useTicks, startSound, finishSound, startMessageKey, finishMessageKey);
        this.effects = effects;
    }

    @Override
    protected boolean applyUseEffect(ItemStack stack, Level level, ServerPlayer player) {
        for (EffectEntry entry : effects) {
            MobEffect effect = entry.effect().get();
            if (effect == null) {
                continue;
            }
            player.addEffect(new MobEffectInstance(effect, entry.durationTicks(), entry.amplifier(),
                    false, true, true), player);
        }
        return true;
    }

    public record EffectEntry(Supplier<MobEffect> effect, int durationTicks, int amplifier) {
    }
}
