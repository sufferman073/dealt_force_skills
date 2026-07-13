package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class FearHudOverlay {
    private FearHudOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }
        int stacks = ClientFearState.stacks();
        if (stacks <= 0) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        String text = Component.translatable("hud.dealt_force_skills.fear", stacks).getString();
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - 58;
        graphics.drawCenteredString(font, text, x, y, 0xFFB24BFF);
    }
}
