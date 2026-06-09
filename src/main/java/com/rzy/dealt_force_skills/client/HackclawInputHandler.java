package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawTool;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawToolAction;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import com.rzy.dealt_force_skills.client.character.ClientHackclawHudState;
import com.rzy.dealt_force_skills.client.visual.HackclawPathLineRenderer;
import com.rzy.dealt_force_skills.network.C2S_HackclawToolAction;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class HackclawInputHandler {
    private static final int EQUIP_HOLD_TICKS = 8;
    private static boolean active1WasDown;
    private static int active1HeldTicks;
    private static boolean sentKnifeEquip;
    private static boolean suppressKnifeReleaseThrow;
    private static boolean active2WasDown;
    private static int active2HeldTicks;
    private static boolean sentFlashDroneEquip;
    private static boolean suppressFlashDroneReleaseThrow;

    private HackclawInputHandler() {
    }

    public static void tick(Minecraft minecraft) {
        HackclawPathLineRenderer.tick(minecraft);
        if (minecraft.player == null || !ClientHackclawHudState.shouldRender()) {
            resetActive1();
            resetActive2();
            return;
        }

        handleActive1Key();
        handleActive2Key();
        if (ClientHackclawHudState.hasEquippedTool()) {
            releaseBlockedKey(minecraft.options.keyAttack);
            releaseBlockedKey(minecraft.options.keyUse);
            releaseBlockedKey(minecraft.options.keyPickItem);
            releaseBlockedKey(minecraft.options.keyDrop);
            releaseBlockedKey(minecraft.options.keySwapOffhand);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!ClientHackclawHudState.hasEquippedTool() || !isPrimaryOrSecondary(event.getButton())) {
            return;
        }
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        event.setCanceled(true);
        if (event.getAction() == GLFW.GLFW_PRESS) {
            sendEquippedToolAction(event.getButton());
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!ClientHackclawHudState.hasEquippedTool()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.isAttack()) {
            sendEquippedToolAction(GLFW.GLFW_MOUSE_BUTTON_LEFT);
        } else if (event.isUseItem()) {
            sendEquippedToolAction(GLFW.GLFW_MOUSE_BUTTON_RIGHT);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen == null && ClientHackclawHudState.hasEquippedTool()) {
            event.setCanceled(true);
        }
    }

    private static void handleActive1Key() {
        if (KeybindRegister.ACTIVE_SKILL_1 == null) {
            return;
        }

        boolean down = KeybindRegister.ACTIVE_SKILL_1.isDown();
        if (down) {
            active1WasDown = true;
            while (KeybindRegister.ACTIVE_SKILL_1.consumeClick()) {
            }
            active1HeldTicks++;
            if (active1HeldTicks >= EQUIP_HOLD_TICKS && !sentKnifeEquip) {
                sentKnifeEquip = true;
                NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.EQUIP_HACKING_KNIFE));
            }
            return;
        }

        if (active1WasDown) {
            if (sentKnifeEquip && !suppressKnifeReleaseThrow) {
                NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.THROW_HACKING_KNIFE));
            } else if (!sentKnifeEquip) {
                if (ClientHackclawHudState.hasEquippedTool()) {
                    NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.STOW_TOOL));
                } else {
                    ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_1);
                }
            }
        }
        resetActive1();
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
            active2HeldTicks++;
            if (active2HeldTicks >= EQUIP_HOLD_TICKS && !sentFlashDroneEquip) {
                sentFlashDroneEquip = true;
                NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.EQUIP_FLASH_DRONE));
            }
            return;
        }

        if (active2WasDown) {
            if (sentFlashDroneEquip && !suppressFlashDroneReleaseThrow) {
                NetworkHandler.sendToServer(new C2S_HackclawToolAction(
                        HackclawToolAction.THROW_FLASH_DRONE,
                        HackclawPathLineRenderer.highlightedTargetId()));
            } else if (!sentFlashDroneEquip) {
                if (ClientHackclawHudState.hasEquippedTool()) {
                    NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.STOW_TOOL));
                } else {
                    ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_2);
                }
            }
        }
        resetActive2();
    }

    private static void sendEquippedToolAction(int button) {
        if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.STOW_TOOL));
            suppressKnifeReleaseThrow = true;
            suppressFlashDroneReleaseThrow = true;
            return;
        }

        HackclawTool tool = ClientHackclawHudState.equippedTool();
        if (tool == HackclawTool.HACKING_KNIFE) {
            suppressKnifeReleaseThrow = true;
            NetworkHandler.sendToServer(new C2S_HackclawToolAction(HackclawToolAction.THROW_HACKING_KNIFE));
        } else if (tool == HackclawTool.FLASH_DRONE) {
            suppressFlashDroneReleaseThrow = true;
            NetworkHandler.sendToServer(new C2S_HackclawToolAction(
                    HackclawToolAction.THROW_FLASH_DRONE,
                    HackclawPathLineRenderer.highlightedTargetId()));
        }
    }

    private static void resetActive1() {
        active1WasDown = false;
        active1HeldTicks = 0;
        sentKnifeEquip = false;
        suppressKnifeReleaseThrow = false;
    }

    private static void resetActive2() {
        active2WasDown = false;
        active2HeldTicks = 0;
        sentFlashDroneEquip = false;
        suppressFlashDroneReleaseThrow = false;
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
