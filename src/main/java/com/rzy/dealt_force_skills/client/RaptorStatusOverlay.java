package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class RaptorStatusOverlay {
    private RaptorStatusOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        int width = event.getGuiGraphics().guiWidth();
        int height = event.getGuiGraphics().guiHeight();
        if (minecraft.player.hasEffect(ModEffects.RAPTOR_ACTION_PAUSE.get())) {
            event.getGuiGraphics().fill(0, 0, width, height, 0x665B7CFA);
        } else if (minecraft.player.hasEffect(ModEffects.RAPTOR_ELECTROMAGNETIC_INTERFERENCE.get())) {
            int alpha = (minecraft.player.tickCount / 4) % 2 == 0 ? 36 : 60;
            event.getGuiGraphics().fill(0, 0, width, height, (alpha << 24) | 0x00254CFF);
        }
    }
}
