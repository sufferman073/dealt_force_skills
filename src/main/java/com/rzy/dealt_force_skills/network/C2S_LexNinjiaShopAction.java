package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.LexNinjiaShopAction;
import com.rzy.dealt_force_skills.shop.LexNinjiaShopManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_LexNinjiaShopAction(String artId, LexNinjiaShopAction action) {
    public static void encode(C2S_LexNinjiaShopAction msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.artId, 64);
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2S_LexNinjiaShopAction decode(FriendlyByteBuf buf) {
        String artId = buf.readUtf(64);
        int ordinal = buf.readVarInt();
        LexNinjiaShopAction[] values = LexNinjiaShopAction.values();
        LexNinjiaShopAction action = ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : LexNinjiaShopAction.BUY;
        return new C2S_LexNinjiaShopAction(artId, action);
    }

    public static void handle(C2S_LexNinjiaShopAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                LexNinjiaShopManager.handleAction(player, msg.artId, msg.action);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
