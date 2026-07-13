package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedDisassemblyBeamEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_HvkConstructorQuery {
    private final BlockPos pos;
    private final String query;
    private final int page;

    public C2S_HvkConstructorQuery(BlockPos pos, String query, int page) {
        this.pos = pos;
        this.query = query == null ? "" : query;
        this.page = Math.max(0, page);
    }

    public static void encode(C2S_HvkConstructorQuery msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.query);
        buf.writeVarInt(msg.page);
    }

    public static C2S_HvkConstructorQuery decode(FriendlyByteBuf buf) {
        return new C2S_HvkConstructorQuery(buf.readBlockPos(), buf.readUtf(128), buf.readVarInt());
    }

    public static void handle(C2S_HvkConstructorQuery msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(msg.pos);
                if (blockEntity instanceof HvkAdvancedStandardTemplateConstructorBlockEntity constructor) {
                    constructor.sendRecipes(player, msg.query, msg.page);
                } else if (blockEntity instanceof HvkAdvancedDisassemblyBeamEmitterBlockEntity emitter) {
                    emitter.sendRecipes(player, msg.query, msg.page);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
