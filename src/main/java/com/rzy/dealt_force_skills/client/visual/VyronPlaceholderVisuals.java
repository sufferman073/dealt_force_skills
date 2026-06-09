package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.vyron.VyronTool;
import com.rzy.dealt_force_skills.client.VyronInputHandler;
import com.rzy.dealt_force_skills.client.character.ClientVyronHudState;
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
public final class VyronPlaceholderVisuals {
    private VyronPlaceholderVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (!shouldRenderPlaceholder()) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        if (ClientVyronHudState.equippedTool() == VyronTool.TIGER_CANNON) {
            poseStack.translate(0.50D, -0.16D, -0.70D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-24.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-9.0F));
            poseStack.scale(1.16F, 1.16F, 1.16F);
        } else {
            poseStack.translate(0.36D, -0.20D, -0.54D);
            poseStack.mulPose(Axis.YP.rotationDegrees(-16.0F));
            poseStack.mulPose(Axis.XP.rotationDegrees(-18.0F));
            poseStack.scale(1.28F, 1.28F, 1.28F);
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getItemRenderer().renderStatic(
                minecraft.player,
                placeholderStack(),
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
        if (shouldRenderPlaceholder()) {
            event.setCanceled(true);
        }
    }

    private static boolean shouldRenderPlaceholder() {
        return ClientVyronHudState.hasEquippedTool() || VyronInputHandler.isMagneticBombHeldForVisual();
    }

    private static ItemStack placeholderStack() {
        if (ClientVyronHudState.equippedTool() == VyronTool.TIGER_CANNON) {
            return new ItemStack(Items.CROSSBOW);
        }
        return new ItemStack(Items.SLIME_BALL);
    }
}
