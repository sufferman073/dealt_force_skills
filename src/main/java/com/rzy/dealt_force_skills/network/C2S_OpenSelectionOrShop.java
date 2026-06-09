package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.HaffShopManager;
import com.rzy.dealt_force_skills.shop.LexNinjiaShopManager;
import com.rzy.dealt_force_skills.shop.UndeadShopManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_OpenSelectionOrShop {
    public static void encode(C2S_OpenSelectionOrShop msg, FriendlyByteBuf buf) {
    }

    public static C2S_OpenSelectionOrShop decode(FriendlyByteBuf buf) {
        return new C2S_OpenSelectionOrShop();
    }

    public static void handle(C2S_OpenSelectionOrShop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (LexNinjiaShopManager.shouldOpen(player)) {
                LexNinjiaShopManager.open(player);
            } else if (UndeadShopManager.shouldOpen(player)) {
                UndeadShopManager.open(player);
            } else if (HaffShopManager.shouldOpenShop(player)) {
                HaffShopManager.openShop(player);
            } else {
                NetworkHandler.sendToPlayer(new S2C_OpenCharacterSelection(), player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
