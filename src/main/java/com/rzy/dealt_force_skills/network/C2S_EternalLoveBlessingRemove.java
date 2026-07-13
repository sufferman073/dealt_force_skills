package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.item.EternalLoveBlessingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_EternalLoveBlessingRemove {
    private final ResourceLocation effectId;

    public C2S_EternalLoveBlessingRemove(ResourceLocation effectId) {
        this.effectId = effectId;
    }

    public static void encode(C2S_EternalLoveBlessingRemove msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.effectId == null ? "minecraft:speed" : msg.effectId.toString());
    }

    public static C2S_EternalLoveBlessingRemove decode(FriendlyByteBuf buf) {
        ResourceLocation id;
        try {
            id = ResourceLocation.parse(buf.readUtf(512));
        } catch (RuntimeException ignored) {
            id = ResourceLocation.fromNamespaceAndPath("minecraft", "speed");
        }
        return new C2S_EternalLoveBlessingRemove(id);
    }

    public static void handle(C2S_EternalLoveBlessingRemove msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                EternalLoveBlessingItem.removeRecordFromAnyBlessing(player, msg.effectId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
