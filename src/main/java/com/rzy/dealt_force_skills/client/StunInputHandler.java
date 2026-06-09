package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class StunInputHandler {
    private StunInputHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (isDowned()) {
            drainDownedKeys();
            return;
        }
        if (!isStunned()) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        drainKey(KeybindRegister.CHARACTER_SELECT);
        drainKey(KeybindRegister.ACTIVE_SKILL_1);
        drainKey(KeybindRegister.ACTIVE_SKILL_2);
        drainKey(KeybindRegister.CORE_SKILL);
        drainKey(minecraft.options.keyAttack);
        drainKey(minecraft.options.keyUse);
        drainKey(minecraft.options.keyPickItem);
        drainKey(minecraft.options.keyDrop);
        drainKey(minecraft.options.keySwapOffhand);
        drainKey(minecraft.options.keyInventory);
        drainKey(minecraft.options.keyUp);
        drainKey(minecraft.options.keyDown);
        drainKey(minecraft.options.keyLeft);
        drainKey(minecraft.options.keyRight);
        drainKey(minecraft.options.keyJump);
        drainKey(minecraft.options.keyShift);
        if (minecraft.screen != null) {
            minecraft.setScreen(null);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (isStunned() || (isDowned() && Minecraft.getInstance().screen == null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (isStunned() || (isDowned() && Minecraft.getInstance().screen == null)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!isStunned() && !isDowned()) {
            return;
        }
        event.setSwingHand(false);
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (isStunned() && event.getNewScreen() != null) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (isDowned()) {
            event.getInput().jumping = false;
            return;
        }
        if (!isStunned()) {
            return;
        }
        event.getInput().forwardImpulse = 0.0f;
        event.getInput().leftImpulse = 0.0f;
        event.getInput().up = false;
        event.getInput().down = false;
        event.getInput().left = false;
        event.getInput().right = false;
        event.getInput().jumping = false;
        event.getInput().shiftKeyDown = false;
    }

    private static boolean isStunned() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && minecraft.player.isAlive()
                && minecraft.player.hasEffect(ModEffects.STUN.get());
    }

    private static boolean isDowned() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null
                && minecraft.player.isAlive()
                && minecraft.player.hasEffect(ModEffects.STINGER_DOWNED.get());
    }

    private static void drainDownedKeys() {
        Minecraft minecraft = Minecraft.getInstance();
        drainKey(KeybindRegister.CHARACTER_SELECT);
        drainKey(KeybindRegister.ACTIVE_SKILL_1);
        drainKey(KeybindRegister.ACTIVE_SKILL_2);
        drainKey(KeybindRegister.CORE_SKILL);
        drainKey(minecraft.options.keyAttack);
        drainKey(minecraft.options.keyUse);
        drainKey(minecraft.options.keyPickItem);
        drainKey(minecraft.options.keyDrop);
        drainKey(minecraft.options.keySwapOffhand);
        drainKey(minecraft.options.keyInventory);
        drainKey(minecraft.options.keyJump);
    }

    private static void drainKey(KeyMapping keyMapping) {
        if (keyMapping == null) {
            return;
        }
        keyMapping.setDown(false);
        while (keyMapping.consumeClick()) {
            // Suppress queued controls while stun owns the player.
        }
    }
}
