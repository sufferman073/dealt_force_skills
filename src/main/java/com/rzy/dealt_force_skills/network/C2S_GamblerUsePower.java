package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.gambler.GamblerStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_GamblerUsePower {
    private final int powerOrdinal;

    public C2S_GamblerUsePower(int powerOrdinal) {
        this.powerOrdinal = powerOrdinal;
    }

    public static void encode(C2S_GamblerUsePower msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.powerOrdinal);
    }

    public static C2S_GamblerUsePower decode(FriendlyByteBuf buf) {
        return new C2S_GamblerUsePower(buf.readVarInt());
    }

    public static void handle(C2S_GamblerUsePower msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                GamblerStateManager.useStoredPower(player, msg.powerOrdinal);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
