package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroTool;
import com.rzy.dealt_force_skills.client.character.ClientNikaidouHiroHudState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class NikaidouHiroHeldItemVisuals {
    private NikaidouHiroHeldItemVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (!ClientNikaidouHiroHudState.hasEquippedTool()) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        NikaidouHiroTool tool = ClientNikaidouHiroHudState.equippedTool();
        if (tool == NikaidouHiroTool.RITUAL_SWORD) {
            poseStack.translate(0.42D, -0.30D, -0.68D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-28.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-34.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(18.0F));
            poseStack.scale(1.12F, 1.12F, 1.12F);
        } else {
            poseStack.translate(0.45D, -0.20D, -0.58D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-22.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-24.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(10.0F));
            poseStack.scale(0.86F, 0.86F, 0.86F);
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemRenderer().renderStatic(
                minecraft.player,
                new ItemStack(tool == NikaidouHiroTool.RITUAL_SWORD ? Items.END_ROD : Items.BLAZE_ROD),
                ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                false,
                poseStack,
                event.getMultiBufferSource(),
                minecraft.level,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY,
                0
        );
        poseStack.popPose();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderArm(RenderArmEvent event) {
        if (ClientNikaidouHiroHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }
}
