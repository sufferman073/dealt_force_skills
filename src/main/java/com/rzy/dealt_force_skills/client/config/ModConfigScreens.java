package com.rzy.dealt_force_skills.client.config;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.screen.DealtForceConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Registers the mod-list "Config" button for Forge 1.20.1.
 * Layout is inspired by Cloth Config categories/fields, implemented in-tree without a hard dependency.
 */
@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModConfigScreens {
    private ModConfigScreens() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (minecraft, parent) -> new DealtForceConfigScreen(parent))));
    }
}
