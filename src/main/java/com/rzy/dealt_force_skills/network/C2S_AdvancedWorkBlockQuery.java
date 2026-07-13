package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.HvkClonePrototypeBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedTreasureCompassBlockEntity;
import com.rzy.dealt_force_skills.block.InterdimensionalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_AdvancedWorkBlockQuery {
    private final BlockPos pos;
    private final int blockType;
    private final int mode;
    private final String query;
    private final int page;

    public C2S_AdvancedWorkBlockQuery(BlockPos pos, int blockType, int mode, String query, int page) {
        this.pos = pos;
        this.blockType = blockType;
        this.mode = mode;
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
    }

    public static void encode(C2S_AdvancedWorkBlockQuery msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.blockType);
        buf.writeVarInt(msg.mode);
        buf.writeUtf(msg.query);
        buf.writeVarInt(msg.page);
    }

    public static C2S_AdvancedWorkBlockQuery decode(FriendlyByteBuf buf) {
        return new C2S_AdvancedWorkBlockQuery(buf.readBlockPos(), buf.readVarInt(), buf.readVarInt(),
                buf.readUtf(128), buf.readVarInt());
    }

    public static void handle(C2S_AdvancedWorkBlockQuery msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            BlockEntity blockEntity = player.level().getBlockEntity(msg.pos);
            if (blockEntity instanceof HvkClonePrototypeBlockEntity clone) {
                clone.sendData(player, msg.mode, msg.query, msg.page);
            } else if (blockEntity instanceof HvkAdvancedTreasureCompassBlockEntity compass) {
                compass.sendData(player, msg.mode, msg.query, msg.page);
            } else if (blockEntity instanceof InterdimensionalBlockEntity interdimensional) {
                interdimensional.sendData(player, msg.mode, msg.query, msg.page);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
