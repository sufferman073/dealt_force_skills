package com.rzy.dealt_force_skills.event;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.block.HvkAdvancedDisassemblyBeamEmitterBlockEntity;
import com.rzy.dealt_force_skills.block.HvkAdvancedStandardTemplateConstructorBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class HvkRecipeCatalogEvents {
    private HvkRecipeCatalogEvents() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        warm(event.getServer(), true);
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel level) {
            HvkAdvancedStandardTemplateConstructorBlockEntity.warmRecipeCatalog(level);
            HvkAdvancedDisassemblyBeamEmitterBlockEntity.warmRecipeCatalog(level);
        }
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        if (event.getPlayer() == null) {
            warm(event.getPlayerList().getServer(), true);
        }
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        HvkAdvancedStandardTemplateConstructorBlockEntity.clearRecipeCatalog(event.getServer());
        HvkAdvancedDisassemblyBeamEmitterBlockEntity.clearRecipeCatalog(event.getServer());
    }

    private static void warm(MinecraftServer server, boolean clearFirst) {
        if (server == null) {
            return;
        }
        if (clearFirst) {
            HvkAdvancedStandardTemplateConstructorBlockEntity.clearRecipeCatalog(server);
            HvkAdvancedDisassemblyBeamEmitterBlockEntity.clearRecipeCatalog(server);
        }
        ServerLevel level = server.overworld();
        HvkAdvancedStandardTemplateConstructorBlockEntity.warmRecipeCatalog(level);
        HvkAdvancedDisassemblyBeamEmitterBlockEntity.warmRecipeCatalog(level);
    }
}
