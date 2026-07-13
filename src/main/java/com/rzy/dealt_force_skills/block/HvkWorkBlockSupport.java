package com.rzy.dealt_force_skills.block;

import com.rzy.dealt_force_skills.registry.ModBlocks;
import com.rzy.dealt_force_skills.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

final class HvkWorkBlockSupport {
    private static final int EXCLUSION_RADIUS = 6;

    private HvkWorkBlockSupport() {
    }

    static boolean shouldPreventConstructorPlacement(Level level, BlockPos pos, LivingEntity placer) {
        if (!hasEmitterNearby(level, pos)) {
            return false;
        }
        if (!level.isClientSide && placer instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(
                    "message.dealt_force_skills.hvk_constructor.repelled_by_disassembler"), true);
        }
        return true;
    }

    static void repelConstructorsAroundEmitter(Level level, BlockPos emitterPos, LivingEntity placer) {
        if (level.isClientSide) {
            return;
        }
        List<BlockPos> constructors = new ArrayList<>();
        for (BlockPos candidate : BlockPos.betweenClosed(
                emitterPos.offset(-EXCLUSION_RADIUS, -EXCLUSION_RADIUS, -EXCLUSION_RADIUS),
                emitterPos.offset(EXCLUSION_RADIUS, EXCLUSION_RADIUS, EXCLUSION_RADIUS))) {
            BlockState state = level.getBlockState(candidate);
            if (state.is(ModBlocks.HVK_ADVANCED_STANDARD_TEMPLATE_CONSTRUCTOR.get())) {
                constructors.add(candidate.immutable());
            }
        }
        if (constructors.isEmpty()) {
            return;
        }
        for (BlockPos constructorPos : constructors) {
            level.destroyBlock(constructorPos, false);
        }
        if (placer instanceof ServerPlayer player) {
            player.displayClientMessage(Component.translatable(
                    "message.dealt_force_skills.hvk_constructor.ran_away"), true);
            player.playNotifySound(ModSounds.HVK_DISASSEMBLER_REPEL.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    private static boolean hasEmitterNearby(Level level, BlockPos center) {
        for (BlockPos candidate : BlockPos.betweenClosed(
                center.offset(-EXCLUSION_RADIUS, -EXCLUSION_RADIUS, -EXCLUSION_RADIUS),
                center.offset(EXCLUSION_RADIUS, EXCLUSION_RADIUS, EXCLUSION_RADIUS))) {
            if (level.getBlockState(candidate).is(ModBlocks.HVK_ADVANCED_DISASSEMBLY_BEAM_EMITTER.get())) {
                return true;
            }
        }
        return false;
    }
}
