package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import com.rzy.dealt_force_skills.client.visual.ClientToolReleaseAction;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class SinevaInputHandler {
    private static final int BLADE_WIRE_HOLD_TICKS = DealtForceConfig.intValue("client.sineva_input_handler.blade_wire_hold_ticks", 10);
    private static final int GRAPPLE_RELEASE_TICKS = DealtForceConfig.intValue("client.sineva_input_handler.grapple_release_ticks", 3);

    private static boolean active1WasDown;
    private static int active1HeldTicks;
    private static boolean bladeWireHeld;
    /** Set when blade wire is cancelled by mouse click; prevents re-entering hold until key is fully released. */
    private static boolean bladeWireCancelled;

    private SinevaInputHandler() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !ClientSinevaHudState.shouldRender()) {
            resetBladeWireInput();
            return;
        }
        if (KeybindRegister.ACTIVE_SKILL_1 == null) {
            return;
        }

        // Always consume clicks to prevent ClientEvents from double-firing
        while (KeybindRegister.ACTIVE_SKILL_1.consumeClick()) {
            // Consumed by SinevaInputHandler exclusively.
        }

        boolean active1Down = KeybindRegister.ACTIVE_SKILL_1.isDown();
        if (active1Down) {
            if (bladeWireCancelled) {
                // Key is still held after a cancel - ignore until fully released
                return;
            }
            active1HeldTicks++;
            active1WasDown = true;
            if (active1HeldTicks >= BLADE_WIRE_HOLD_TICKS) {
                bladeWireHeld = true;
            }
            return;
        }

        // Key was released
        if (bladeWireCancelled) {
            // Key released after cancel - reset cancelled state
            bladeWireCancelled = false;
            active1WasDown = false;
            active1HeldTicks = 0;
            return;
        }

        if (active1WasDown) {
            if (bladeWireHeld) {
                // Release while blade wire is held = THROW
                throwHeldBladeWire();
            } else if (active1HeldTicks < BLADE_WIRE_HOLD_TICKS) {
                // Short tap = normal skill use (place blade wire at feet)
                ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_1);
            }
        }
        active1WasDown = false;
        active1HeldTicks = 0;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!bladeWireHeld || !ClientSinevaHudState.shouldRender()) {
            return;
        }
        if (event.getButton() != GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getButton() != GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            return;
        }

        event.setCanceled(true);
        if (Minecraft.getInstance().screen != null || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        // Both left and right click cancel the blade wire hold
        cancelBladeWire();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!bladeWireHeld || !ClientSinevaHudState.shouldRender()) {
            return;
        }

        event.setSwingHand(false);
        event.setCanceled(true);
        // Both attack and use cancel the blade wire hold
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            cancelBladeWire();
        }
    }

    private static void throwHeldBladeWire() {
        ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_1, true);
        bladeWireHeld = false;
        active1WasDown = false;
        active1HeldTicks = 0;
    }

    private static void cancelBladeWire() {
        bladeWireHeld = false;
        bladeWireCancelled = true; // Prevent re-entering hold until key is fully released
        active1WasDown = false;
        active1HeldTicks = 0;
    }

    private static void resetBladeWireInput() {
        active1WasDown = false;
        active1HeldTicks = 0;
        bladeWireHeld = false;
        bladeWireCancelled = false;
    }

    public static void startGrappleUse() {
        if (!ClientSinevaHudState.shouldRender()) {
            return;
        }
        if (ClientToolReleaseAction.schedule(
                "sineva.grapple.fire",
                GRAPPLE_RELEASE_TICKS,
                () -> ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_2))) {
            com.rzy.dealt_force_skills.client.visual.SinevaPlaceholderVisuals.startGrappleFire();
        }
    }

    /** Whether the player is currently holding the blade wire ready to throw. */
    public static boolean isBladeWireHeld() {
        return bladeWireHeld && ClientSinevaHudState.shouldRender();
    }
}
