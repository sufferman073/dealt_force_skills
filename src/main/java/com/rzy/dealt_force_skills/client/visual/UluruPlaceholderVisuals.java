package com.rzy.dealt_force_skills.client.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.uluru.UluruTool;
import com.rzy.dealt_force_skills.client.UluruMissileController;
import com.rzy.dealt_force_skills.client.character.ClientUluruHudState;
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
public final class UluruPlaceholderVisuals {
    private UluruPlaceholderVisuals() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRenderHand(RenderHandEvent event) {
        if (UluruMissileController.isControlling()) {
            event.setCanceled(true);
            return;
        }
        if (!ClientUluruHudState.hasEquippedTool()) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.42d, -0.22d, -0.62d);
        poseStack.mulPose(Axis.YP.rotationDegrees(-25.0f));
        poseStack.mulPose(Axis.XP.rotationDegrees(-12.0f));
        poseStack.scale(1.0f, 1.0f, 1.0f);

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
        if (UluruMissileController.isControlling() || ClientUluruHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }

    private static ItemStack placeholderStack() {
        return switch (ClientUluruHudState.equippedTool()) {
            case INCENDIARY -> new ItemStack(Items.FIRE_CHARGE);
            case COVER -> new ItemStack(Items.SCAFFOLDING);
            case MISSILE -> new ItemStack(Items.CROSSBOW);
            case NONE -> ItemStack.EMPTY;
        };
    }
}
