package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.team.DealtTeamEvents;
import com.rzy.dealt_force_skills.team.DealtTeamManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_TeamSpectatorCycle {
    private final int direction;

    public C2S_TeamSpectatorCycle(int direction) {
        this.direction = Integer.compare(direction, 0);
    }

    public static void encode(C2S_TeamSpectatorCycle msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.direction);
    }

    public static C2S_TeamSpectatorCycle decode(FriendlyByteBuf buf) {
        return new C2S_TeamSpectatorCycle(buf.readVarInt());
    }

    public static void handle(C2S_TeamSpectatorCycle msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && msg.direction != 0) {
                DealtTeamManager.cycleSpectatorTarget(player, msg.direction)
                        .ifPresent(DealtTeamEvents::applySpectatorTarget);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
