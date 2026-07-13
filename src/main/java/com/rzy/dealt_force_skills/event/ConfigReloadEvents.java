package com.rzy.dealt_force_skills.event;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.config.ConfigHotReload;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class ConfigReloadEvents {
    private static final ResourceManagerReloadListener LISTENER = resourceManager -> ConfigHotReload.reloadAll();

    private ConfigReloadEvents() {
    }

    @SubscribeEvent
    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(LISTENER);
    }
}
