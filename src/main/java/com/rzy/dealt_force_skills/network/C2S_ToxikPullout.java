package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.toxik.ToxikStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_ToxikPullout {
    private final boolean active;

    public C2S_ToxikPullout(boolean active) {
        this.active = active;
    }

    public static void encode(C2S_ToxikPullout msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
    }

    public static C2S_ToxikPullout decode(FriendlyByteBuf buf) {
        return new C2S_ToxikPullout(buf.readBoolean());
    }

    public static void handle(C2S_ToxikPullout msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && !player.isSpectator()) {
                ToxikStateManager.setFireflyPulloutActive(player, msg.active);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
