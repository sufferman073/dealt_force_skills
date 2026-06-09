package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.UndeadShopAction;
import com.rzy.dealt_force_skills.shop.UndeadShopManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_UndeadShopAction(String entryId, UndeadShopAction action) {
    public static void encode(C2S_UndeadShopAction msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.entryId, 64);
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2S_UndeadShopAction decode(FriendlyByteBuf buf) {
        String entryId = buf.readUtf(64);
        int ordinal = buf.readVarInt();
        UndeadShopAction[] values = UndeadShopAction.values();
        UndeadShopAction action = ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : UndeadShopAction.BUY;
        return new C2S_UndeadShopAction(entryId, action);
    }

    public static void handle(C2S_UndeadShopAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                UndeadShopManager.handleAction(player, msg.entryId, msg.action);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
