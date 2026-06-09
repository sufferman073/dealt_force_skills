package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.stinger.StingerTool;
import com.rzy.dealt_force_skills.client.character.ClientStingerHudState;
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
public final class StingerPlaceholderVisuals {
    private StingerPlaceholderVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (!ClientStingerHudState.hasEquippedTool()) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        StingerTool tool = ClientStingerHudState.equippedTool();
        if (tool == StingerTool.STIM_GUN) {
            poseStack.translate(0.48D, -0.18D, -0.66D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-22.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-10.0F));
            poseStack.scale(1.05F, 1.05F, 1.05F);
        } else if (tool == StingerTool.SMOKE_DRONE) {
            poseStack.translate(0.38D, -0.18D, -0.58D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-18.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
        } else {
            poseStack.translate(0.36D, -0.18D, -0.54D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-16.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
            poseStack.scale(0.82F, 0.82F, 0.82F);
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
        if (ClientStingerHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }

    private static ItemStack placeholderStack(StingerTool tool) {
        return switch (tool) {
            case SMOKE_GRENADE -> new ItemStack(Items.GRAY_DYE);
            case SMOKE_DRONE -> new ItemStack(Items.BEEHIVE);
            case STIM_GUN -> new ItemStack(Items.CROSSBOW);
            case NONE -> ItemStack.EMPTY;
        };
    }
}
