package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.gambler.GamblerStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class C2S_GamblerDuelInvite {
    private static final int MAX_INVITES = 64;

    private final List<UUID> invitedIds;

    public C2S_GamblerDuelInvite(List<UUID> invitedIds) {
        this.invitedIds = invitedIds == null ? List.of() : List.copyOf(invitedIds);
    }

    public static void encode(C2S_GamblerDuelInvite msg, FriendlyByteBuf buf) {
        int count = Math.min(MAX_INVITES, msg.invitedIds.size());
        buf.writeVarInt(count);
        for (int i = 0; i < count; i++) {
            buf.writeUUID(msg.invitedIds.get(i));
        }
    }

    public static C2S_GamblerDuelInvite decode(FriendlyByteBuf buf) {
        int count = Math.min(MAX_INVITES, Math.max(0, buf.readVarInt()));
        List<UUID> ids = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ids.add(buf.readUUID());
        }
        return new C2S_GamblerDuelInvite(ids);
    }

    public static void handle(C2S_GamblerDuelInvite msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                GamblerStateManager.confirmFinalBetInvite(player, msg.invitedIds);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
