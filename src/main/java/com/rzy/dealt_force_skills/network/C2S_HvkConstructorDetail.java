package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedDisassemblyBeamEmitterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_HvkConstructorDetail {
    private final BlockPos pos;
    private final ResourceLocation recipeId;
    private final int requestId;

    public C2S_HvkConstructorDetail(BlockPos pos, ResourceLocation recipeId) {
        this(pos, recipeId, 0);
    }

    public C2S_HvkConstructorDetail(BlockPos pos, ResourceLocation recipeId, int requestId) {
        this.pos = pos;
        this.recipeId = recipeId == null ? ResourceLocation.fromNamespaceAndPath("minecraft", "air") : recipeId;
        this.requestId = Math.max(0, requestId);
    }

    public static void encode(C2S_HvkConstructorDetail msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.recipeId.toString());
        buf.writeVarInt(msg.requestId);
    }

    public static C2S_HvkConstructorDetail decode(FriendlyByteBuf buf) {
        return new C2S_HvkConstructorDetail(buf.readBlockPos(), readResourceLocation(buf), buf.readVarInt());
    }

    public static void handle(C2S_HvkConstructorDetail msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                BlockEntity blockEntity = player.level().getBlockEntity(msg.pos);
                if (blockEntity instanceof HvkAdvancedStandardTemplateConstructorBlockEntity constructor) {
                    constructor.sendRecipeDetail(player, msg.recipeId, msg.requestId);
                } else if (blockEntity instanceof HvkAdvancedDisassemblyBeamEmitterBlockEntity emitter) {
                    emitter.sendRecipeDetail(player, msg.recipeId, msg.requestId);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    private static ResourceLocation readResourceLocation(FriendlyByteBuf buf) {
        String value = buf.readUtf(512);
        try {
            return ResourceLocation.parse(value);
        } catch (RuntimeException ex) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", "air");
        }
    }
}
