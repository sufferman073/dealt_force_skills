package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class ClientSinevaHudState {
    private static boolean synced;
    private static int bladeWireCharges;
    private static int bladeWireMaxCharges;
    private static int bladeWireRechargeTicks;
    private static int grappleCooldownTicks;
    private static int bombSuitCooldownTicks;
    private static int bombSuitEquipTicks;
    private static int bashCooldownTicks;
    private static int chargeCooldownTicks;
    private static boolean bombSuitActive;
    private static boolean shieldDeployed;
    private static int viewportHealth;
    private static int viewportMaxHealth;

    private ClientSinevaHudState() {
    }

    public static void reset() {
        synced = false;
        bladeWireCharges = 0;
        bladeWireMaxCharges = 0;
        bladeWireRechargeTicks = 0;
        grappleCooldownTicks = 0;
        bombSuitCooldownTicks = 0;
        bombSuitEquipTicks = 0;
        bashCooldownTicks = 0;
        chargeCooldownTicks = 0;
        bombSuitActive = false;
        shieldDeployed = false;
        viewportHealth = 0;
        viewportMaxHealth = 0;
    }

    public static void sync(
            int bladeWireCharges,
            int bladeWireMaxCharges,
            int bladeWireRechargeTicks,
            int grappleCooldownTicks,
            int bombSuitCooldownTicks,
            int bombSuitEquipTicks,
            int bashCooldownTicks,
            int chargeCooldownTicks,
            boolean bombSuitActive,
            boolean shieldDeployed,
            int viewportHealth,
            int viewportMaxHealth
    ) {
        synced = true;
        ClientSinevaHudState.bladeWireCharges = bladeWireCharges;
        ClientSinevaHudState.bladeWireMaxCharges = bladeWireMaxCharges;
        ClientSinevaHudState.bladeWireRechargeTicks = bladeWireRechargeTicks;
        ClientSinevaHudState.grappleCooldownTicks = grappleCooldownTicks;
        ClientSinevaHudState.bombSuitCooldownTicks = bombSuitCooldownTicks;
        ClientSinevaHudState.bombSuitEquipTicks = bombSuitEquipTicks;
        ClientSinevaHudState.bashCooldownTicks = bashCooldownTicks;
        ClientSinevaHudState.chargeCooldownTicks = chargeCooldownTicks;
        ClientSinevaHudState.bombSuitActive = bombSuitActive;
        ClientSinevaHudState.shieldDeployed = shieldDeployed;
        ClientSinevaHudState.viewportHealth = viewportHealth;
        ClientSinevaHudState.viewportMaxHealth = viewportMaxHealth;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.SINEVA_ID);
    }

    public static void tick() {
        if (bashCooldownTicks > 0) {
            bashCooldownTicks--;
        }
        if (chargeCooldownTicks > 0) {
            chargeCooldownTicks--;
        }
    }

    public static int bladeWireCharges() {
        return bladeWireCharges;
    }

    public static int bladeWireMaxCharges() {
        return bladeWireMaxCharges;
    }

    public static int bladeWireRechargeTicks() {
        return bladeWireRechargeTicks;
    }

    public static int grappleCooldownTicks() {
        return grappleCooldownTicks;
    }

    public static int bombSuitCooldownTicks() {
        return bombSuitCooldownTicks;
    }

    public static int bombSuitEquipTicks() {
        return bombSuitEquipTicks;
    }

    public static int bashCooldownTicks() {
        return bashCooldownTicks;
    }

    public static int chargeCooldownTicks() {
        return chargeCooldownTicks;
    }

    public static void startBashCooldown(int ticks) {
        bashCooldownTicks = Math.max(bashCooldownTicks, ticks);
    }

    public static void startChargeCooldown(int ticks) {
        chargeCooldownTicks = Math.max(chargeCooldownTicks, ticks);
    }

    public static boolean bombSuitActive() {
        return bombSuitActive;
    }

    public static boolean shieldDeployed() {
        return shieldDeployed;
    }

    public static int viewportHealth() {
        return viewportHealth;
    }

    public static int viewportMaxHealth() {
        return viewportMaxHealth;
    }

    public static void consumeShieldBlockStamina(int percent, int floorPercent) {
        Player player = Minecraft.getInstance().player;
        ParcoolStaminaBridge.consumeLocalPercentAboveFloor(player, percent, floorPercent);
    }
}
