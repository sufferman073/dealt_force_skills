package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.character.toxik.ToxikTool;
import com.rzy.dealt_force_skills.character.toxik.ToxikToolAction;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import com.rzy.dealt_force_skills.client.character.ClientToxikHudState;
import com.rzy.dealt_force_skills.network.C2S_ToxikPullout;
import com.rzy.dealt_force_skills.network.C2S_ToxikToolAction;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.registry.ModEffects;
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
public final class ToxikInputHandler {
    private static final int TEAR_GAS_EQUIP_HOLD_TICKS = 8;
    private static final int CORE_LONG_HOLD_TICKS = 15;

    private static boolean active1WasDown;
    private static int active1HeldTicks;
    private static boolean active2WasDown;
    private static int active2HeldTicks;
    private static boolean sentTearGasEquip;
    private static boolean coreWasDown;
    private static int coreHeldTicks;
    private static boolean pulloutActiveSent;

    private ToxikInputHandler() {
    }

    public static void tick(Minecraft minecraft) {
        handleFireflyPullout(minecraft);

        if (minecraft.player == null || !ClientToxikHudState.shouldRender()) {
            resetActive1();
            resetActive2();
            resetCore();
            return;
        }

        handleActive1Key();
        handleActive2Key();
        handleCoreKey();
        if (ClientToxikHudState.hasEquippedTool()) {
            releaseBlockedKey(minecraft.options.keyAttack);
            releaseBlockedKey(minecraft.options.keyUse);
            releaseBlockedKey(minecraft.options.keyPickItem);
            releaseBlockedKey(minecraft.options.keyDrop);
            releaseBlockedKey(minecraft.options.keySwapOffhand);
        }
    }

    public static boolean ownsCoreSkill() {
        return ClientToxikHudState.shouldRender();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!ClientToxikHudState.hasEquippedTool() || !isPrimaryOrSecondary(event.getButton())) {
            return;
        }
        if (Minecraft.getInstance().screen != null) {
            return;
        }
        event.setCanceled(true);
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        ToxikTool tool = ClientToxikHudState.equippedTool();
        if (tool == ToxikTool.TEAR_GAS) {
            NetworkHandler.sendToServer(new C2S_ToxikToolAction(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                    ? ToxikToolAction.THROW_TEAR_GAS
                    : ToxikToolAction.STOW_TOOL));
            return;
        }
        if (tool == ToxikTool.FIREFLY_SWARM) {
            NetworkHandler.sendToServer(new C2S_ToxikToolAction(event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT
                    ? ToxikToolAction.RELEASE_FIREFLY
                    : ToxikToolAction.TOGGLE_FIREFLY_MODE));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!ClientToxikHudState.hasEquippedTool()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        ToxikTool tool = ClientToxikHudState.equippedTool();
        if (event.isAttack()) {
            if (tool == ToxikTool.TEAR_GAS) {
                NetworkHandler.sendToServer(new C2S_ToxikToolAction(ToxikToolAction.THROW_TEAR_GAS));
            } else if (tool == ToxikTool.FIREFLY_SWARM) {
                NetworkHandler.sendToServer(new C2S_ToxikToolAction(ToxikToolAction.RELEASE_FIREFLY));
            }
        } else if (event.isUseItem()) {
            if (tool == ToxikTool.FIREFLY_SWARM) {
                NetworkHandler.sendToServer(new C2S_ToxikToolAction(ToxikToolAction.TOGGLE_FIREFLY_MODE));
            } else {
                NetworkHandler.sendToServer(new C2S_ToxikToolAction(ToxikToolAction.STOW_TOOL));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen == null && ClientToxikHudState.hasEquippedTool()) {
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
            active1HeldTicks++;
            while (KeybindRegister.ACTIVE_SKILL_1.consumeClick()) {
            }
            return;
        }
        if (active1WasDown) {
            ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_1);
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
            if (active2HeldTicks >= TEAR_GAS_EQUIP_HOLD_TICKS && !sentTearGasEquip) {
                sentTearGasEquip = true;
                NetworkHandler.sendToServer(new C2S_ToxikToolAction(ToxikToolAction.EQUIP_TEAR_GAS));
            }
            return;
        }
        if (active2WasDown && active2HeldTicks < TEAR_GAS_EQUIP_HOLD_TICKS) {
            ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_2);
        }
        resetActive2();
    }

    private static void handleCoreKey() {
        if (KeybindRegister.CORE_SKILL == null) {
            return;
        }
        boolean down = KeybindRegister.CORE_SKILL.isDown();
        if (down) {
            coreWasDown = true;
            coreHeldTicks++;
            while (KeybindRegister.CORE_SKILL.consumeClick()) {
            }
            return;
        }
        if (coreWasDown) {
            if (ClientToxikHudState.equippedTool() == ToxikTool.FIREFLY_SWARM && coreHeldTicks >= CORE_LONG_HOLD_TICKS) {
                NetworkHandler.sendToServer(new C2S_ToxikToolAction(ToxikToolAction.TOGGLE_FIREFLY_MODE));
            } else {
                ClientCharacterSelectionState.useSkill(SkillSlot.CORE, coreHeldTicks >= CORE_LONG_HOLD_TICKS);
            }
        }
        resetCore();
    }

    private static void handleFireflyPullout(Minecraft minecraft) {
        if (minecraft.player == null || minecraft.screen != null || KeybindRegister.ACTIVE_SKILL_1 == null) {
            sendPulloutIfChanged(false);
            return;
        }
        boolean hasFirefly = minecraft.player.hasEffect(ModEffects.TOXIK_FIREFLY_INTERFERENCE.get());
        boolean holdingAttack = hasFirefly && minecraft.options.keyAttack.isDown();
        sendPulloutIfChanged(holdingAttack);
    }

    private static void sendPulloutIfChanged(boolean active) {
        if (pulloutActiveSent == active) {
            return;
        }
        pulloutActiveSent = active;
        NetworkHandler.sendToServer(new C2S_ToxikPullout(active));
    }

    private static void resetActive1() {
        active1WasDown = false;
        active1HeldTicks = 0;
    }

    private static void resetActive2() {
        active2WasDown = false;
        active2HeldTicks = 0;
        sentTearGasEquip = false;
    }

    private static void resetCore() {
        coreWasDown = false;
        coreHeldTicks = 0;
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
