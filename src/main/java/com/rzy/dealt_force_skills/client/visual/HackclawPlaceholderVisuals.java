package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawTool;
import com.rzy.dealt_force_skills.client.character.ClientHackclawHudState;
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
public final class HackclawPlaceholderVisuals {
    private HackclawPlaceholderVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (!ClientHackclawHudState.hasEquippedTool()) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        HackclawTool tool = ClientHackclawHudState.equippedTool();
        if (tool == HackclawTool.HACKING_KNIFE) {
            poseStack.translate(0.46D, -0.18D, -0.58D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-28.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-16.0F));
            poseStack.mulPose(Axis.ZP.rotationDegrees(8.0F));
            poseStack.scale(1.05F, 1.05F, 1.05F);
        } else if (tool == HackclawTool.FLASH_DRONE) {
            poseStack.translate(0.42D, -0.20D, -0.64D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-18.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-20.0F));
            poseStack.scale(0.78F, 0.78F, 0.78F);
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemRenderer().renderStatic(
                minecraft.player,
                placeholderStack(tool),
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
        if (ClientHackclawHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }

    private static ItemStack placeholderStack(HackclawTool tool) {
        return switch (tool) {
            case HACKING_KNIFE -> new ItemStack(Items.IRON_SWORD);
            case FLASH_DRONE -> new ItemStack(Items.DAYLIGHT_DETECTOR);
            case NONE -> ItemStack.EMPTY;
        };
    }
}
