package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.gambler.GamblerStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_GamblerUseTargetPower {
    private final int powerOrdinal;
    private final int targetEntityId;

    public C2S_GamblerUseTargetPower(int powerOrdinal, int targetEntityId) {
        this.powerOrdinal = powerOrdinal;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_GamblerUseTargetPower msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.powerOrdinal);
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_GamblerUseTargetPower decode(FriendlyByteBuf buf) {
        return new C2S_GamblerUseTargetPower(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(C2S_GamblerUseTargetPower msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && !player.isSpectator()) {
                GamblerStateManager.useStoredPower(player, msg.powerOrdinal, msg.targetEntityId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
