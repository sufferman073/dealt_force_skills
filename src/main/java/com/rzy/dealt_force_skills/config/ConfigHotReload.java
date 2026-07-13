package com.rzy.dealt_force_skills.config;

import com.rzy.dealt_force_skills.character.CharacterAvailability;
import com.rzy.dealt_force_skills.character.CharacterBranchPackManager;
import com.rzy.dealt_force_skills.shop.DfsShopCatalog;
import com.rzy.dealt_force_skills.shop.GhrothArmoryCatalog;
import com.rzy.dealt_force_skills.network.C2S_RequestConfigSnapshot;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraftforge.server.ServerLifecycleHooks;

/**
 * Hot-reloads mod TOML configuration without invoking the full vanilla datapack {@code /reload}.
 */
public final class ConfigHotReload {
    private ConfigHotReload() {
    }

    public static Result reloadAll() {
        DealtForceConfig.clearRuntimeOverrides();
        boolean gameplay = DealtForceConfig.reload();
        boolean bosses = DealtBossesConfig.reload();
        boolean player = DealtForcePlayerConfig.reload();
        boolean shop = DealtForceShopConfig.reload();
        boolean shopSet = DealtShopSetConfig.reload();
        boolean gambler = GamblerArenaConfig.reload();
        boolean liveValues = DealtForceConfig.refreshLiveValues();

        CharacterBranchPackManager.verifyGeneratedDataPacks();
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            ResourceManager resourceManager = server.getResourceManager();
            CharacterBranchPackManager.reload(resourceManager);
            DfsShopCatalog.entries();
            GhrothArmoryCatalog.entries();
            DealtForcePlayerConfig.applyToAll(server);
            for (var playerEntity : server.getPlayerList().getPlayers()) {
                CharacterAvailability.syncToClient(playerEntity);
                C2S_RequestConfigSnapshot.sendSnapshot(playerEntity, playerEntity.hasPermissions(2));
            }
        } else {
            CharacterBranchPackManager.reload();
            DfsShopCatalog.entries();
            GhrothArmoryCatalog.entries();
        }
        DealtForceShopConfig.flush();
        DealtShopSetConfig.flush();
        GamblerArenaConfig.flush();

        boolean ok = gameplay && bosses && player && shop && shopSet && gambler && liveValues;
        return new Result(ok, gameplay, bosses, player, shop, shopSet, gambler, liveValues);
    }

    public record Result(
            boolean success,
            boolean gameplay,
            boolean bosses,
            boolean player,
            boolean shop,
            boolean shopSet,
            boolean gambler,
            boolean liveValues
    ) {
    }
}
