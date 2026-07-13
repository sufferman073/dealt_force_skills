package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.HaffShopManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_BuyHaffShopItem {
    private final String entryId;

    public C2S_BuyHaffShopItem(String entryId) {
        this.entryId = entryId == null ? "" : entryId;
    }

    public static void encode(C2S_BuyHaffShopItem msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.entryId);
    }

    public static C2S_BuyHaffShopItem decode(FriendlyByteBuf buf) {
        return new C2S_BuyHaffShopItem(buf.readUtf(256));
    }

    public static void handle(C2S_BuyHaffShopItem msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                HaffShopManager.buy(player, msg.entryId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
