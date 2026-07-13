package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.visual.CharacterSkillModelVisuals;
import com.rzy.dealt_force_skills.client.visual.HeldToolThirdPersonVisuals;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class TeamSpectatorOverlay {
    private TeamSpectatorOverlay() {
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        if (!VanillaGuiOverlay.HOTBAR.id().equals(event.getOverlay().id())) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || !minecraft.player.isSpectator() || minecraft.options.hideGui
                || !ClientTeamSpectatorState.isManaged()) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        String targetName = ClientTeamSpectatorState.targetName();
        if (!targetName.isBlank()) {
            String label = "◀ A   " + targetName + "   D ▶";
            graphics.drawCenteredString(minecraft.font, label, graphics.guiWidth() / 2,
                    Math.max(4, graphics.guiHeight() - 44), 0xFFFFFFFF);
        }

        renderHeldItem(graphics, ClientTeamSpectatorState.mainHand(), graphics.guiWidth() / 2 + 22,
                graphics.guiHeight() - 24);
        renderHeldItem(graphics, ClientTeamSpectatorState.offHand(), graphics.guiWidth() / 2 - 38,
                graphics.guiHeight() - 24);

        int targetEntityId = ClientTeamSpectatorState.targetEntityId();
        if (targetEntityId >= 0) {
            float partialTick = minecraft.getFrameTime();
            if (!CharacterSkillModelVisuals.renderSpectatorFirstPerson(graphics, targetEntityId, partialTick)) {
                HeldToolThirdPersonVisuals.renderSpectatorFirstPerson(graphics, targetEntityId, partialTick);
            }
        }
    }

    private static void renderHeldItem(GuiGraphics graphics, ItemStack stack, int x, int y) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        graphics.renderItem(stack, x, y);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
    }
}
