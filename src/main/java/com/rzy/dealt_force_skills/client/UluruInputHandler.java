package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.uluru.UluruTool;
import com.rzy.dealt_force_skills.client.character.ClientUluruHudState;
import com.rzy.dealt_force_skills.network.C2S_UluruToolAction;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class UluruInputHandler {
    private UluruInputHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!shouldReplaceMouse() || !isPrimaryOrSecondary(event.getButton())) {
            return;
        }

        event.setCanceled(true);
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null) {
            return;
        }

        if (ClientUluruHudState.equippedTool() == UluruTool.MISSILE
                && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            ClientUluruHudState.setMissileAiming(event.getAction() != GLFW.GLFW_RELEASE);
            return;
        }

        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            NetworkHandler.sendToServer(new C2S_UluruToolAction(false, ClientUluruHudState.missileAiming()));
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            NetworkHandler.sendToServer(new C2S_UluruToolAction(true, false));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!ClientUluruHudState.hasEquippedTool()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.isAttack()) {
            NetworkHandler.sendToServer(new C2S_UluruToolAction(false, ClientUluruHudState.missileAiming()));
        } else if (event.isUseItem()) {
            NetworkHandler.sendToServer(new C2S_UluruToolAction(true, false));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (ClientUluruHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (UluruMissileController.isControlling()
                || ClientUluruHudState.equippedTool() != UluruTool.MISSILE
                || !ClientUluruHudState.missileAiming()) {
            return;
        }
        event.setFOV(event.getFOV() * 0.55d);
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !ClientUluruHudState.hasEquippedTool()) {
            ClientUluruHudState.setMissileAiming(false);
            return;
        }

        releaseBlockedKey(minecraft.options.keyAttack);
        releaseBlockedKey(minecraft.options.keyUse);
        releaseBlockedKey(minecraft.options.keyPickItem);
    }

    private static boolean shouldReplaceMouse() {
        Minecraft minecraft = Minecraft.getInstance();
        return ClientUluruHudState.hasEquippedTool() && minecraft.screen == null;
    }

    private static boolean isPrimaryOrSecondary(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    private static void releaseBlockedKey(KeyMapping keyMapping) {
        keyMapping.setDown(false);
        while (keyMapping.consumeClick()) {
            // Drain vanilla clicks while a pseudo tool is equipped.
        }
    }
}
