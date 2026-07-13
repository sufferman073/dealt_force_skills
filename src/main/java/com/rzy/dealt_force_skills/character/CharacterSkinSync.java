package com.rzy.dealt_force_skills.character;

import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncPlayerCharacterSkin;
import com.rzy.dealt_force_skills.registry.ModGameRules;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class CharacterSkinSync {
    private CharacterSkinSync() {
    }

    public static void syncToTracking(ServerPlayer player) {
        NetworkHandler.sendToTrackingAndSelf(packetFor(player), player);
    }

    public static void syncOneTo(ServerPlayer selectedPlayer, ServerPlayer recipient) {
        NetworkHandler.sendToPlayer(packetFor(selectedPlayer), recipient);
    }

    public static void syncAllTo(ServerPlayer recipient) {
        MinecraftServer server = recipient.getServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncOneTo(player, recipient);
        }
    }

    public static void syncAll(MinecraftServer server) {
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            syncToTracking(player);
        }
    }

    private static S2C_SyncPlayerCharacterSkin packetFor(ServerPlayer player) {
        boolean skinsEnabled = ModGameRules.areCharacterSkinsEnabled(player);
        String characterId = CharacterSelectionManager.getSelectedCharacterId(player).orElse("");
        return new S2C_SyncPlayerCharacterSkin(player.getId(), characterId, skinsEnabled);
    }
}
