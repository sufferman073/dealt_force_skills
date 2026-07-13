package com.rzy.dealt_force_skills.item;

import com.rzy.dealt_force_skills.boss.BeaconSummonManager;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeaconBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * HVK brain unit used to summon Beacon boss on an activated beacon pyramid.
 * Activation requires sneak + right-click and works in Survival/Adventure/Creative
 * (spectator excluded). Adventure mode is intentionally supported.
 */
public class HvkBrainUnitItem extends QualityTooltipItem {
    public HvkBrainUnitItem(Item.Properties properties, DfsItemQuality quality, String tooltipKey) {
        super(properties, quality, tooltipKey);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        return tryActivate(context.getLevel(), player, context.getHand(), context.getItemInHand(), context.getClickedPos());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        BlockHitResult hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (hit.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        }
        InteractionResult result = tryActivate(level, player, hand, stack, hit.getBlockPos());
        if (result.consumesAction()) {
            return new InteractionResultHolder<>(result, stack);
        }
        return InteractionResultHolder.pass(stack);
    }

    /**
     * Shared entry used by item callbacks and adventure-mode interact events.
     *
     * @return true when the summon was started (or client-predicted success)
     */
    public static boolean tryActivateFromEvent(Level level, Player player, InteractionHand hand, ItemStack stack, BlockPos pos) {
        if (player == null || stack == null || stack.isEmpty() || !player.isShiftKeyDown()) {
            return false;
        }
        InteractionResult result = tryActivate(level, player, hand, stack, pos);
        return result.consumesAction() || result == InteractionResult.SUCCESS;
    }

    private static InteractionResult tryActivate(Level level, Player player, InteractionHand hand, ItemStack stack, BlockPos pos) {
        if (player == null || stack == null || stack.isEmpty() || pos == null) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockState(pos).getBlock() instanceof BeaconBlock)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.FAIL;
        }
        if (serverPlayer.isSpectator() || serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR) {
            return InteractionResult.FAIL;
        }
        if (!BeaconSummonManager.tryStartSummon(serverPlayer, serverLevel, pos)) {
            return InteractionResult.FAIL;
        }
        if (!serverPlayer.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) {
                serverPlayer.setItemInHand(hand, ItemStack.EMPTY);
            }
        }
        return InteractionResult.CONSUME;
    }
}
