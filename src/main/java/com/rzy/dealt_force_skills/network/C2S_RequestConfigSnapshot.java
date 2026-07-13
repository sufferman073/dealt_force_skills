package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.config.ConfigAccess;
import com.rzy.dealt_force_skills.config.ConfigEntryData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public final class C2S_RequestConfigSnapshot {
    private static final int CHUNK_SIZE = 80;

    public static void encode(C2S_RequestConfigSnapshot msg, FriendlyByteBuf buf) {
    }

    public static C2S_RequestConfigSnapshot decode(FriendlyByteBuf buf) {
        return new C2S_RequestConfigSnapshot();
    }

    public static void handle(C2S_RequestConfigSnapshot msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (!player.hasPermissions(2)) {
                player.sendSystemMessage(Component.translatable("config.dealt_force_skills.error.no_permission"));
                NetworkHandler.sendToPlayer(new S2C_ConfigSnapshot(0, 0, false, true, List.of()), player);
                return;
            }
            sendSnapshot(player, true);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sendSnapshot(ServerPlayer player, boolean canEdit) {
            List<ConfigEntryData> all = ConfigAccess.snapshotAllServerFiles();
            if (all.isEmpty()) {
                NetworkHandler.sendToPlayer(new S2C_ConfigSnapshot(0, 1, canEdit, true, List.of()), player);
                return;
            }
            int totalChunks = (all.size() + CHUNK_SIZE - 1) / CHUNK_SIZE;
            for (int chunk = 0; chunk < totalChunks; chunk++) {
                int from = chunk * CHUNK_SIZE;
                int to = Math.min(all.size(), from + CHUNK_SIZE);
                List<ConfigEntryData> slice = new ArrayList<>(all.subList(from, to));
                NetworkHandler.sendToPlayer(
                        new S2C_ConfigSnapshot(chunk, totalChunks, canEdit, chunk == totalChunks - 1, slice),
                        player);
            }
    }
}
