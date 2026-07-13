package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.HvkClonePrototypeBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedTreasureCompassBlockEntity;
import com.rzy.dealt_force_skills.block.InterdimensionalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_AdvancedWorkBlockAction {
    private final BlockPos pos;
    private final int blockType;
    private final int mode;
    private final ResourceLocation id;
    private final int action;
    private final int count;
    private final String query;
    private final int page;

    public C2S_AdvancedWorkBlockAction(BlockPos pos, int blockType, int mode, ResourceLocation id, int action,
                                       int count, String query, int page) {
        this.pos = pos;
        this.blockType = blockType;
        this.mode = mode;
        this.id = id;
        this.action = action;
        this.count = Math.max(1, count);
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
    }

    public static void encode(C2S_AdvancedWorkBlockAction msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.blockType);
        buf.writeVarInt(msg.mode);
        buf.writeUtf(msg.id == null ? "minecraft:air" : msg.id.toString());
        buf.writeVarInt(msg.action);
        buf.writeVarInt(msg.count);
        buf.writeUtf(msg.query);
        buf.writeVarInt(msg.page);
    }

    public static C2S_AdvancedWorkBlockAction decode(FriendlyByteBuf buf) {
        return new C2S_AdvancedWorkBlockAction(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                S2C_OpenAdvancedWorkBlock.readResourceLocation(buf), buf.readVarInt(), buf.readVarInt(),
                buf.readUtf(128), buf.readVarInt());
    }

    public static void handle(C2S_AdvancedWorkBlockAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(msg.pos);
            if (blockEntity instanceof HvkClonePrototypeBlockEntity clone) {
                clone.performAction(player, msg.mode, msg.id, msg.action, msg.count, msg.query, msg.page);
            } else if (blockEntity instanceof HvkAdvancedTreasureCompassBlockEntity compass) {
                compass.performAction(player, msg.mode, msg.id, msg.action, msg.count, msg.query, msg.page);
            } else if (blockEntity instanceof InterdimensionalBlockEntity interdimensional) {
                interdimensional.performAction(player, msg.mode, msg.id, msg.action, msg.count, msg.query, msg.page);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
