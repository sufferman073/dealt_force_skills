package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.ntwo.NTwoStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_NTwoFreezeStruggle {
    private final boolean holding;

    public C2S_NTwoFreezeStruggle(boolean holding) {
        this.holding = holding;
    }

    public static void encode(C2S_NTwoFreezeStruggle msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.holding);
    }

    public static C2S_NTwoFreezeStruggle decode(FriendlyByteBuf buf) {
        return new C2S_NTwoFreezeStruggle(buf.readBoolean());
    }

    public static void handle(C2S_NTwoFreezeStruggle msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                NTwoStateManager.setFreezeStruggleHolding(player, msg.holding);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
