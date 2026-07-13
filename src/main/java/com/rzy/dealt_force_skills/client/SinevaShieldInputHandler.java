package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import com.rzy.dealt_force_skills.client.visual.ClientSinevaVisualState;
import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import com.rzy.dealt_force_skills.network.C2S_SinevaShieldBash;
import com.rzy.dealt_force_skills.network.C2S_SinevaShieldCharge;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public final class SinevaShieldInputHandler {
    private static volatile int BASH_STAMINA_PERCENT_COST = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("BASH_STAMINA_PERCENT_COST", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("client.sineva_shield_input_handler.bash_stamina_percent_cost", 20));
    private static volatile int CHARGE_STAMINA_PERCENT_COST = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("CHARGE_STAMINA_PERCENT_COST", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("client.sineva_shield_input_handler.charge_stamina_percent_cost", 30));
    private static volatile int BASH_ACTIVE_STAMINA_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("BASH_ACTIVE_STAMINA_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("client.sineva_shield_input_handler.bash_active_stamina_ticks", 20));
    private static int lockedHotbarSlot = -1;

    private SinevaShieldInputHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseButtonPre(InputEvent.MouseButton.Pre event) {
        if (!shouldBlockMouseButtons() || !isBlockedMouseButton(event.getButton())) {
            return;
        }

        event.setCanceled(true);

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen != null || event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }

        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            triggerBash();
        } else if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            triggerCharge();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKey(InputEvent.InteractionKeyMappingTriggered event) {
        if (!shouldBlockVanillaInventory()) {
            return;
        }

        event.setSwingHand(false);
        event.setCanceled(true);

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (event.isAttack()) {
            triggerBash();
        } else if (event.isUseItem()) {
            triggerCharge();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (shouldBlockVanillaInventory()) {
            event.setCanceled(true);
        }
    }

    public static void tick(Minecraft minecraft) {
        if (minecraft.player == null || !shouldBlockVanillaInventory()) {
            lockedHotbarSlot = -1;
            return;
        }

        if (minecraft.screen instanceof AbstractContainerScreen<?>) {
            minecraft.setScreen(null);
        }

        releaseBlockedKey(minecraft.options.keyAttack);
        releaseBlockedKey(minecraft.options.keyUse);
        releaseBlockedKey(minecraft.options.keyPickItem);

        if (lockedHotbarSlot < 0) {
            lockedHotbarSlot = minecraft.player.getInventory().selected;
        }

        if (minecraft.player.getInventory().selected != lockedHotbarSlot) {
            minecraft.player.getInventory().selected = lockedHotbarSlot;
            if (minecraft.player.connection != null) {
                minecraft.player.connection.send(new ServerboundSetCarriedItemPacket(lockedHotbarSlot));
            }
        }
    }

    private static boolean shouldBlockVanillaInventory() {
        return ClientSinevaHudState.shouldRender() && ClientSinevaHudState.shieldDeployed();
    }

    private static boolean shouldBlockMouseButtons() {
        if (!shouldBlockVanillaInventory()) {
            return false;
        }
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.screen == null || minecraft.screen instanceof AbstractContainerScreen<?>;
    }

    private static boolean isBlockedMouseButton(int button) {
        return button == GLFW.GLFW_MOUSE_BUTTON_LEFT
                || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT
                || button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
    }

    private static void triggerBash() {
        if (ClientSinevaHudState.bashCooldownTicks() > 0) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (!consumeStamina(player, BASH_STAMINA_PERCENT_COST, BASH_ACTIVE_STAMINA_TICKS)) {
            return;
        }

        ClientSinevaHudState.startBashCooldown(10);
        ClientSinevaVisualState.startBashAnimation();
        NetworkHandler.sendToServer(new C2S_SinevaShieldBash());
    }

    private static void triggerCharge() {
        if (ClientSinevaHudState.chargeCooldownTicks() > 0) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null || !hasHorizontalLook(player)) {
            return;
        }
        if (!consumeStamina(player, CHARGE_STAMINA_PERCENT_COST, 0)) {
            return;
        }

        ClientSinevaHudState.startChargeCooldown(20);
        ClientSinevaVisualState.startChargeAnimation();
        NetworkHandler.sendToServer(new C2S_SinevaShieldCharge());
    }

    private static boolean hasHorizontalLook(Player player) {
        Vec3 look = player.getLookAngle();
        return look.x * look.x + look.z * look.z >= 0.001;
    }

    private static boolean consumeStamina(Player player, int percent, int activeTicks) {
        ParcoolStaminaBridge.ConsumeResult result = ParcoolStaminaBridge.consumeLocalPercent(player, percent);
        if (result == ParcoolStaminaBridge.ConsumeResult.SUCCESS) {
            ParcoolStaminaBridge.markActiveStaminaUse(player, activeTicks);
            return true;
        }

        if (player != null) {
            player.displayClientMessage(Component.translatable(result == ParcoolStaminaBridge.ConsumeResult.NOT_ENOUGH
                    ? "message.dealt_force_skills.sineva.not_enough_stamina"
                    : "message.dealt_force_skills.sineva.parcool_stamina_unavailable"), true);
        }
        return false;
    }

    private static void releaseBlockedKey(KeyMapping keyMapping) {
        keyMapping.setDown(false);
        while (keyMapping.consumeClick()) {
            // Drain queued clicks so held shield state cannot trigger item actions on the next tick.
        }
    }
}
