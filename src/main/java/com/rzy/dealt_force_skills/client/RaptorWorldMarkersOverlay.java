package com.rzy.dealt_force_skills.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientRaptorHudState;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class RaptorWorldMarkersOverlay {
    private RaptorWorldMarkersOverlay() {
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        Camera camera = minecraft.gameRenderer.getMainCamera();
        Vec3 cameraPos = camera.getPosition();
        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Font font = minecraft.font;

        RenderSystem.disableDepthTest();
        if (ClientRaptorHudState.shouldRender()) {
            for (var marker : ClientRaptorHudState.footprints()) {
                Vec3 pos = marker.position().add(0.0D, 0.12D, 0.0D);
                String text = Component.translatable("hud.dealt_force_skills.raptor.footprint",
                        marker.ownerName(), Math.max(0, marker.ageTicks() / 20)).getString();
                renderMarker(poseStack, buffers, font, camera, cameraPos, pos, text, 0xFF9EC9FF);
            }
        }
        for (Map.Entry<Integer, Vec3> entry : ClientRaptorHudState.revealedPositions().entrySet()) {
            Vec3 pos = entry.getValue().add(0.0D, 1.05D, 0.0D);
            double distance = pos.distanceTo(minecraft.player.getEyePosition());
            String text = Component.translatable("hud.dealt_force_skills.raptor.revealed").getString()
                    + " " + Math.round(distance) + "m";
            renderMarker(poseStack, buffers, font, camera, cameraPos, pos, text, 0xFF7EE8FF);
        }
        buffers.endBatch();
        RenderSystem.enableDepthTest();
    }

    private static void renderMarker(
            PoseStack poseStack,
            MultiBufferSource.BufferSource buffers,
            Font font,
            Camera camera,
            Vec3 cameraPos,
            Vec3 pos,
            String text,
            int color
    ) {
        poseStack.pushPose();
        poseStack.translate(pos.x - cameraPos.x, pos.y - cameraPos.y, pos.z - cameraPos.z);
        poseStack.mulPose(camera.rotation());
        poseStack.scale(-0.025F, -0.025F, 0.025F);
        font.drawInBatch(text, -font.width(text) / 2.0F, 0.0F, color, false,
                poseStack.last().pose(), buffers, Font.DisplayMode.SEE_THROUGH,
                0x66000000, LightTexture.FULL_BRIGHT);
        poseStack.popPose();
    }
}
