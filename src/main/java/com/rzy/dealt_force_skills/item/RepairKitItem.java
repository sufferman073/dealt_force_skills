package com.rzy.dealt_force_skills.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Supplier;

public class RepairKitItem extends QualityTooltipItem {
    private static final int USE_DURATION = 72_000;
    private static final int REPAIR_INTERVAL_TICKS = 20;
    private static final int BASE_COST = 100;
    private static final int BASE_REPAIR = 100;
    private static final String TAG_USE_START_TICK = "DfsRepairUseStartTick";

    private final EquipmentSlot targetSlot;
    private final int windupTicks;
    private final Supplier<SoundEvent> startSound;
    private final Supplier<SoundEvent> workSound;
    private final Supplier<SoundEvent> finishSound;
    private final String startMessageKey;
    private final String workMessageKey;
    private final String failMessageKey;

    public RepairKitItem(Properties properties,
                         DfsItemQuality quality,
                         String tooltipKey,
                         EquipmentSlot targetSlot,
                         int windupTicks,
                         Supplier<SoundEvent> startSound,
                         Supplier<SoundEvent> workSound,
                         Supplier<SoundEvent> finishSound,
                         String startMessageKey,
                         String workMessageKey,
                         String failMessageKey) {
        super(properties, quality, tooltipKey);
        this.targetSlot = targetSlot;
        this.windupTicks = windupTicks;
        this.startSound = startSound;
        this.workSound = workSound;
        this.finishSound = finishSound;
        this.startMessageKey = startMessageKey;
        this.workMessageKey = workMessageKey;
        this.failMessageKey = failMessageKey;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!canRepair(player)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(failMessageKey), true);
            }
            return InteractionResultHolder.fail(stack);
        }
        player.startUsingItem(hand);
        if (!level.isClientSide) {
            stack.getOrCreateTag().putLong(TAG_USE_START_TICK, level.getGameTime());
            level.playSound(null, player.blockPosition(), startSound.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            player.displayClientMessage(Component.translatable(startMessageKey), true);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
            return;
        }
        int elapsed = elapsedUseTicks(level, stack, remainingUseDuration);
        if (elapsed < windupTicks || (elapsed - windupTicks) % REPAIR_INTERVAL_TICKS != 0) {
            return;
        }
        if (!repairOnePulse(player, stack)) {
            player.stopUsingItem();
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeCharged) {
        if (!level.isClientSide && entity instanceof ServerPlayer player) {
            clearUseStart(stack);
            level.playSound(null, player.blockPosition(), finishSound.get(), SoundSource.PLAYERS, 0.65F, 1.0F);
        }
    }

    private int elapsedUseTicks(Level level, ItemStack stack, int remainingUseDuration) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_USE_START_TICK)) {
            tag.putLong(TAG_USE_START_TICK, level.getGameTime());
            return Math.max(0, USE_DURATION - Math.max(0, remainingUseDuration));
        }
        long elapsed = level.getGameTime() - tag.getLong(TAG_USE_START_TICK);
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, elapsed));
    }

    private void clearUseStart(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag != null) {
            tag.remove(TAG_USE_START_TICK);
        }
    }

    private boolean canRepair(Player player) {
        ItemStack target = player.getItemBySlot(targetSlot);
        return !target.isEmpty() && target.isDamageableItem() && target.isDamaged();
    }

    private boolean repairOnePulse(ServerPlayer player, ItemStack kit) {
        ItemStack target = player.getItemBySlot(targetSlot);
        if (target.isEmpty() || !target.isDamageableItem() || !target.isDamaged()) {
            player.displayClientMessage(Component.translatable(failMessageKey), true);
            return false;
        }
        int cost = adjustedCost(target);
        int repair = adjustedRepair(target);
        if (!player.getAbilities().instabuild) {
            int available = kit.getMaxDamage() - kit.getDamageValue();
            if (available <= 0) {
                return false;
            }
            int actualCost = Math.min(cost, available);
            repair = Math.max(1, Math.round(repair * (actualCost / (float) cost)));
            kit.hurtAndBreak(actualCost, player, broken -> broken.broadcastBreakEvent(player.getUsedItemHand()));
        }
        target.setDamageValue(Math.max(0, target.getDamageValue() - repair));
        player.level().playSound(null, player.blockPosition(), workSound.get(), SoundSource.PLAYERS, 0.65F, 1.0F);
        player.displayClientMessage(Component.translatable(workMessageKey, repair), true);
        return !kit.isEmpty() && target.isDamaged();
    }

    private int adjustedCost(ItemStack target) {
        int delta = quality().tier() - DfsItemQuality.of(target).tier();
        if (delta <= 0) {
            return BASE_COST;
        }
        return Math.max(1, Math.round(BASE_COST * Math.max(0.2F, 1.0F - delta * 0.2F)));
    }

    private int adjustedRepair(ItemStack target) {
        int delta = DfsItemQuality.of(target).tier() - quality().tier();
        if (delta <= 0) {
            return BASE_REPAIR;
        }
        return Math.max(1, Math.round((float) (BASE_REPAIR * Math.pow(0.7D, delta))));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    protected void addExtraTooltip(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.dealt_force_skills.repair_windup",
                String.format(java.util.Locale.ROOT, "%.1f", windupTicks / 20.0D)));
    }
}
