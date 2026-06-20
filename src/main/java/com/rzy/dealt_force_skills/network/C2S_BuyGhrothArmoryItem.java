package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.GhrothArmoryManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_BuyGhrothArmoryItem {
    private final String entryId;

    public C2S_BuyGhrothArmoryItem(String entryId) {
        this.entryId = entryId == null ? "" : entryId;
    }

    public static void encode(C2S_BuyGhrothArmoryItem msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.entryId);
    }

    public static C2S_BuyGhrothArmoryItem decode(FriendlyByteBuf buf) {
        return new C2S_BuyGhrothArmoryItem(buf.readUtf(256));
    }

    public static void handle(C2S_BuyGhrothArmoryItem msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                GhrothArmoryManager.buy(player, msg.entryId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
