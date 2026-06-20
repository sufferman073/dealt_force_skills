package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.SaeedRecruitManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_OpenSaeedMonitor {
    public static void encode(C2S_OpenSaeedMonitor msg, FriendlyByteBuf buf) {
    }

    public static C2S_OpenSaeedMonitor decode(FriendlyByteBuf buf) {
        return new C2S_OpenSaeedMonitor();
    }

    public static void handle(C2S_OpenSaeedMonitor msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SaeedRecruitManager.openMonitor(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
