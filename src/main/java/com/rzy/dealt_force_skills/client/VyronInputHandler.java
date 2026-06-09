package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.vyron.VyronTool;
import com.rzy.dealt_force_skills.character.vyron.VyronToolAction;
import com.rzy.dealt_force_skills.client.character.ClientVyronHudState;
import com.rzy.dealt_force_skills.network.C2S_VyronDash;
import com.rzy.dealt_force_skills.network.C2S_VyronToolAction;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class VyronInputHandler {
    private static final int BOMB_HOLD_EQUIP_TICKS = 8;
    private static boolean active2WasDown;
    private static int active2HeldTicks;
    private static boolean sentBombEquip;
    private static boolean canceledBombHold;

    private VyronInputHandler() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !ClientVyronHudState.shouldRender()) {
            resetActive2();
            return;
        }

        while (KeybindRegister.ACTIVE_SKILL_1 != null && KeybindRegister.ACTIVE_SKILL_1.consumeClick()) {
            Vec3 direction = movementDirection(minecraft);
            NetworkHandler.sendToServer(new C2S_VyronDash(direction.x, direction.z));
        }

        handleActive2Key();
        if (ClientVyronHudState.hasEquippedTool() || isMagneticBombHeldForVisual()) {
            releaseBlockedKey(minecraft.options.keyAttack);
            releaseBlockedKey(minecraft.options.keyUse);
            releaseBlockedKey(minecraft.options.keyPickItem);
            releaseBlockedKey(minecraft.options.keyDrop);
            releaseBlockedKey(minecraft.options.keySwapOffhand);
        }
    }

    public static boolean isMagneticBombHeldForVisual() {
        return ClientVyronHudState.shouldRender() && active2WasDown && !canceledBombHold && active2HeldTicks >= BOMB_HOLD_EQUIP_TICKS;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!ClientVyronHudState.hasEquippedTool() || !isPrimaryOrSecondary(event.getButton())) {
            return;
        }
        event.setCanceled(true);
        if (Minecraft.getInstance().screen != null || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (ClientVyronHudState.equippedTool() == VyronTool.TIGER_CANNON && event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            NetworkHandler.sendToServer(new C2S_VyronToolAction(VyronToolAction.FIRE_TIGER_CANNON));
        } else {
            if (ClientVyronHudState.equippedTool() == VyronTool.MAGNETIC_BOMB) {
                cancelMagneticBombHold();
            }
            NetworkHandler.sendToServer(new C2S_VyronToolAction(VyronToolAction.STOW_TOOL));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!ClientVyronHudState.hasEquippedTool()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (ClientVyronHudState.equippedTool() == VyronTool.TIGER_CANNON && event.isAttack()) {
            NetworkHandler.sendToServer(new C2S_VyronToolAction(VyronToolAction.FIRE_TIGER_CANNON));
        } else {
            if (ClientVyronHudState.equippedTool() == VyronTool.MAGNETIC_BOMB) {
                cancelMagneticBombHold();
            }
            NetworkHandler.sendToServer(new C2S_VyronToolAction(VyronToolAction.STOW_TOOL));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (ClientVyronHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (ClientVyronHudState.shouldRender() && ClientVyronHudState.dashTicks() > 0) {
            event.setFOV(event.getFOV() * 1.33D);
        }
    }

    private static void handleActive2Key() {
        if (KeybindRegister.ACTIVE_SKILL_2 == null) {
            return;
        }

        boolean down = KeybindRegister.ACTIVE_SKILL_2.isDown();
        if (down) {
            active2WasDown = true;
            while (KeybindRegister.ACTIVE_SKILL_2.consumeClick()) {
            }
            if (canceledBombHold) {
                return;
            }
            active2HeldTicks++;
            if (active2HeldTicks >= BOMB_HOLD_EQUIP_TICKS && !sentBombEquip) {
                sentBombEquip = true;
                NetworkHandler.sendToServer(new C2S_VyronToolAction(VyronToolAction.EQUIP_MAGNETIC_BOMB));
            }
            return;
        }

        if (active2WasDown && !canceledBombHold) {
            boolean highThrow = active2HeldTicks >= BOMB_HOLD_EQUIP_TICKS;
            NetworkHandler.sendToServer(new C2S_VyronToolAction(VyronToolAction.THROW_MAGNETIC_BOMB, highThrow));
        }
        resetActive2();
    }

    private static Vec3 movementDirection(Minecraft minecraft) {
        float forward = 0.0f;
        float strafe = 0.0f;
        if (minecraft.options.keyUp.isDown()) forward += 1.0f;
        if (minecraft.options.keyDown.isDown()) forward -= 1.0f;
        if (minecraft.options.keyLeft.isDown()) strafe += 1.0f;
        if (minecraft.options.keyRight.isDown()) strafe -= 1.0f;

        float yaw = minecraft.player == null ? 0.0f : minecraft.player.getYRot();
        double yawRad = (yaw + 90.0F) * Mth.DEG_TO_RAD;
        Vec3 forwardVec = new Vec3(Math.cos(yawRad), 0.0D, Math.sin(yawRad));
        Vec3 rightVec = new Vec3(forwardVec.z, 0.0D, -forwardVec.x);
        Vec3 result = forwardVec.scale(forward).add(rightVec.scale(strafe));
        if (result.lengthSqr() < 0.0001D && minecraft.player != null) {
            Vec3 look = minecraft.player.getLookAngle();
            result = new Vec3(look.x, 0.0D, look.z);
        }
        return result.lengthSqr() < 0.0001D ? new Vec3(0.0D, 0.0D, 1.0D) : result.normalize();
    }

    private static void resetActive2() {
        active2WasDown = false;
        active2HeldTicks = 0;
        sentBombEquip = false;
        canceledBombHold = false;
    }

    private static void cancelMagneticBombHold() {
        canceledBombHold = true;
        active2HeldTicks = 0;
        sentBombEquip = false;
    }

    private static boolean isPrimaryOrSecondary(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
    }

    private static void releaseBlockedKey(KeyMapping keyMapping) {
        keyMapping.setDown(false);
        while (keyMapping.consumeClick()) {
        }
    }
}
