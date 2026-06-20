package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.character.dwolf.DWolfStateManager;
import com.rzy.dealt_force_skills.character.dwolf.DWolfToolAction;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import com.rzy.dealt_force_skills.client.character.ClientDWolfHudState;
import com.rzy.dealt_force_skills.client.visual.DWolfPlaceholderVisuals;
import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import com.rzy.dealt_force_skills.network.C2S_DWolfSlide;
import com.rzy.dealt_force_skills.network.C2S_DWolfToolAction;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class DWolfInputHandler {
    private static final int SMOKE_HIGH_THROW_HOLD_TICKS = DealtForceConfig.intValue("client.d_wolf_input_handler.smoke_high_throw_hold_ticks", 8);
    private static final int SMOKE_TRIGGER_TICKS = DealtForceConfig.intValue("client.d_wolf_input_handler.smoke_trigger_ticks", 5);
    private static final int CANNON_SHOT_INTERVAL_TICKS = DealtForceConfig.intValue("client.d_wolf_input_handler.cannon_shot_interval_ticks", 5);
    private static final int SLIDE_LOCAL_COOLDOWN_TICKS = DealtForceConfig.intValue("client.d_wolf_input_handler.slide_local_cooldown_ticks", 8);

    private static boolean active2WasDown;
    private static int active2HeldTicks;
    private static boolean slideKeyWasDown;
    private static int localSlideTicks;
    private static int localSlideCooldownTicks;
    private static int pendingSlideRequestTicks;
    private static int overloadRewardHoldTicks;
    private static int pendingSmokeThrowTicks;
    private static boolean pendingSmokeHighThrow;
    private static int pendingHandCannonAnimations;
    private static int nextHandCannonAnimationTicks;

    private DWolfInputHandler() {
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !ClientDWolfHudState.shouldRender()) {
            active2WasDown = false;
            active2HeldTicks = 0;
            slideKeyWasDown = false;
            localSlideTicks = 0;
            localSlideCooldownTicks = 0;
            pendingSlideRequestTicks = 0;
            overloadRewardHoldTicks = 0;
            pendingSmokeThrowTicks = 0;
            pendingSmokeHighThrow = false;
            pendingHandCannonAnimations = 0;
            nextHandCannonAnimationTicks = 0;
            return;
        }

        handlePendingHandCannonAnimations();
        handlePendingSmokeThrow();
        handleSkillKeys();
        handleSlide(minecraft);
        handleOverloadSelfReward(minecraft);
        if (ClientDWolfHudState.hasHandCannonEquipped()) {
            releaseBlockedKey(minecraft.options.keyAttack);
            releaseBlockedKey(minecraft.options.keyUse);
            releaseBlockedKey(minecraft.options.keyPickItem);
        }
        if (localSlideTicks > 0) {
            localSlideTicks--;
        }
        if (localSlideCooldownTicks > 0) {
            localSlideCooldownTicks--;
        }
        if (pendingSlideRequestTicks > 0) {
            pendingSlideRequestTicks--;
        }
    }

    public static void acceptSlide() {
        Player player = Minecraft.getInstance().player;
        if (player == null || !ClientDWolfHudState.shouldRender()) {
            return;
        }

        ParcoolStaminaBridge.ConsumeResult result = ParcoolStaminaBridge.consumeLocalPercent(
                player,
                DWolfStateManager.SLIDE_STAMINA_PERCENT_COST
        );
        if (result != ParcoolStaminaBridge.ConsumeResult.SUCCESS) {
            player.displayClientMessage(Component.translatable(result == ParcoolStaminaBridge.ConsumeResult.NOT_ENOUGH
                    ? "message.dealt_force_skills.d_wolf.not_enough_stamina"
                    : "message.dealt_force_skills.d_wolf.parcool_stamina_unavailable"), true);
        }
        localSlideTicks = DWolfStateManager.SLIDE_TICKS;
        localSlideCooldownTicks = SLIDE_LOCAL_COOLDOWN_TICKS;
        pendingSlideRequestTicks = 0;
    }

    public static boolean isSmokeHeldForVisual() {
        return ClientDWolfHudState.shouldRender() && (active2WasDown || pendingSmokeThrowTicks > 0);
    }

    public static boolean isSmokeHighThrowPreview() {
        return isSmokeHeldForVisual() && active2HeldTicks >= SMOKE_HIGH_THROW_HOLD_TICKS;
    }

    public static boolean isSlideActiveForVisual() {
        return ClientDWolfHudState.shouldRender() && (ClientDWolfHudState.slideTicks() > 0 || localSlideTicks > 0);
    }

    public static boolean ownsCoreSkill() {
        return KeybindRegister.CORE_SKILL != null
                && KeybindRegister.CORE_SKILL.isDown()
                && isOverloadRewardCandidate(Minecraft.getInstance());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!ClientDWolfHudState.hasHandCannonEquipped() || !isPrimaryOrSecondary(event.getButton())) {
            return;
        }

        event.setCanceled(true);
        if (Minecraft.getInstance().screen != null || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (startHandCannonBurstAnimation()) {
                NetworkHandler.sendToServer(new C2S_DWolfToolAction(DWolfToolAction.FIRE_HAND_CANNON));
            }
        } else {
            NetworkHandler.sendToServer(new C2S_DWolfToolAction(DWolfToolAction.STOW_TOOL));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!ClientDWolfHudState.hasHandCannonEquipped()) {
            return;
        }

        event.setSwingHand(false);
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        if (event.isAttack()) {
            if (startHandCannonBurstAnimation()) {
                NetworkHandler.sendToServer(new C2S_DWolfToolAction(DWolfToolAction.FIRE_HAND_CANNON));
            }
        } else if (event.isUseItem()) {
            NetworkHandler.sendToServer(new C2S_DWolfToolAction(DWolfToolAction.STOW_TOOL));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (ClientDWolfHudState.hasHandCannonEquipped()) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (ClientDWolfHudState.slideTicks() <= 0 && localSlideTicks <= 0) {
            return;
        }
        event.getInput().shiftKeyDown = false;
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        if (ClientDWolfHudState.shouldRender() && ClientDWolfHudState.overloadActiveTicks() > 0) {
            double maximum = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue(
                    "client.fov.maximum_degrees", 120.0D);
            double cappedMaximum = Math.max(1.0D, maximum);
            if (event.getFOV() > cappedMaximum) {
                event.setFOV(cappedMaximum);
            }
        }
    }

    private static void handleSkillKeys() {
        while (KeybindRegister.ACTIVE_SKILL_1 != null && KeybindRegister.ACTIVE_SKILL_1.consumeClick()) {
            ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_1);
        }

        if (KeybindRegister.ACTIVE_SKILL_2 == null) {
            return;
        }
        if (pendingSmokeThrowTicks > 0) {
            while (KeybindRegister.ACTIVE_SKILL_2.consumeClick()) {
            }
            active2WasDown = false;
            active2HeldTicks = 0;
            return;
        }

        boolean active2Down = KeybindRegister.ACTIVE_SKILL_2.isDown();
        if (active2Down) {
            active2HeldTicks++;
            active2WasDown = true;
            while (KeybindRegister.ACTIVE_SKILL_2.consumeClick()) {
                // D-Wolf needs release timing to distinguish quick and high smoke throws.
            }
            return;
        }

        if (active2WasDown) {
            pendingSmokeHighThrow = active2HeldTicks >= SMOKE_HIGH_THROW_HOLD_TICKS;
            pendingSmokeThrowTicks = SMOKE_TRIGGER_TICKS;
            DWolfPlaceholderVisuals.startSmokeQuickTrigger();
        }
        active2WasDown = false;
        active2HeldTicks = 0;
    }

    private static void handlePendingSmokeThrow() {
        if (pendingSmokeThrowTicks <= 0) {
            return;
        }
        pendingSmokeThrowTicks--;
        if (pendingSmokeThrowTicks == 0) {
            ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_2, pendingSmokeHighThrow);
            pendingSmokeHighThrow = false;
        }
    }

    private static boolean startHandCannonBurstAnimation() {
        if (pendingHandCannonAnimations > 0
                || ClientDWolfHudState.cannonBurstShots() > 0
                || ClientDWolfHudState.handCannonCharges() <= 0) {
            return false;
        }
        DWolfPlaceholderVisuals.startHandCannonFire();
        pendingHandCannonAnimations = 2;
        nextHandCannonAnimationTicks = CANNON_SHOT_INTERVAL_TICKS;
        return true;
    }

    private static void handlePendingHandCannonAnimations() {
        if (pendingHandCannonAnimations <= 0 || nextHandCannonAnimationTicks <= 0) {
            return;
        }
        nextHandCannonAnimationTicks--;
        if (nextHandCannonAnimationTicks > 0) {
            return;
        }
        DWolfPlaceholderVisuals.startHandCannonFire();
        pendingHandCannonAnimations--;
        nextHandCannonAnimationTicks = pendingHandCannonAnimations > 0
                ? CANNON_SHOT_INTERVAL_TICKS
                : 0;
    }

    private static void handleSlide(Minecraft minecraft) {
        boolean shiftDown = minecraft.options.keyShift.isDown();
        if (!shiftDown) {
            slideKeyWasDown = false;
            return;
        }

        if (!slideKeyWasDown && localSlideTicks <= 0 && localSlideCooldownTicks <= 0 && pendingSlideRequestTicks <= 0 && minecraft.screen == null
                && minecraft.player != null && minecraft.player.isSprinting() && minecraft.player.onGround()) {
            if (hasSlideStamina(minecraft.player)) {
                pendingSlideRequestTicks = 8;
                NetworkHandler.sendToServer(new C2S_DWolfSlide());
            }
        }
        slideKeyWasDown = true;
    }

    private static void handleOverloadSelfReward(Minecraft minecraft) {
        if (!ownsCoreSkill()) {
            overloadRewardHoldTicks = 0;
            return;
        }

        while (KeybindRegister.CORE_SKILL.consumeClick()) {
            // D-Wolf overload self-reward owns long-held core input while stationary.
        }
        overloadRewardHoldTicks = Math.min(20, overloadRewardHoldTicks + 1);
        int filled = Math.max(0, Math.min(10, overloadRewardHoldTicks / 2));
        String bar = "[" + "#".repeat(filled) + ".".repeat(10 - filled) + "]";
        minecraft.player.displayClientMessage(Component.translatable("message.dealt_force_skills.d_wolf.rewarding_self", bar), true);
        if (overloadRewardHoldTicks >= 20) {
            NetworkHandler.sendToServer(new C2S_DWolfToolAction(DWolfToolAction.OVERLOAD_SELF_REWARD));
            overloadRewardHoldTicks = 0;
        }
    }

    private static boolean isOverloadRewardCandidate(Minecraft minecraft) {
        Player player = minecraft.player;
        return player != null
                && minecraft.screen == null
                && ClientDWolfHudState.shouldRender()
                && ClientDWolfHudState.overloadActiveTicks() > 0
                && !ClientDWolfHudState.hasHandCannonEquipped()
                && player.getMainHandItem().isEmpty()
                && player.getOffhandItem().isEmpty()
                && player.getDeltaMovement().horizontalDistanceSqr() <= 0.0025D
                && !minecraft.options.keyUp.isDown()
                && !minecraft.options.keyDown.isDown()
                && !minecraft.options.keyLeft.isDown()
                && !minecraft.options.keyRight.isDown()
                && !minecraft.options.keyJump.isDown()
                && !minecraft.options.keyShift.isDown();
    }

    private static boolean hasSlideStamina(Player player) {
        ParcoolStaminaBridge.ConsumeResult result = ParcoolStaminaBridge.canConsumeLocalPercent(player, DWolfStateManager.SLIDE_STAMINA_PERCENT_COST);
        if (result == ParcoolStaminaBridge.ConsumeResult.SUCCESS) {
            return true;
        }

        player.displayClientMessage(Component.translatable(result == ParcoolStaminaBridge.ConsumeResult.NOT_ENOUGH
                ? "message.dealt_force_skills.d_wolf.not_enough_stamina"
                : "message.dealt_force_skills.d_wolf.parcool_stamina_unavailable"), true);
        return false;
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
