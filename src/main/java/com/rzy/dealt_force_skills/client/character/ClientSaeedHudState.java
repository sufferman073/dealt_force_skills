package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;

public final class ClientSaeedHudState {
    private static boolean synced;
    private static int fireAmmo;
    private static int fireMaxAmmo;
    private static int fireRechargeTicks;
    private static boolean crossbowEquipped;
    private static boolean fireBounce;
    private static int rollCooldownTicks;
    private static int coreCooldownTicks;
    private static int coreActiveTicks;
    private static int commandVisualTicks;

    private ClientSaeedHudState() {
    }

    public static void reset() {
        synced = false;
        fireAmmo = 0;
        fireMaxAmmo = 0;
        fireRechargeTicks = 0;
        crossbowEquipped = false;
        fireBounce = false;
        rollCooldownTicks = 0;
        coreCooldownTicks = 0;
        coreActiveTicks = 0;
        commandVisualTicks = 0;
    }

    public static void sync(
            int fireAmmo,
            int fireMaxAmmo,
            int fireRechargeTicks,
            boolean crossbowEquipped,
            boolean fireBounce,
            int rollCooldownTicks,
            int coreCooldownTicks,
            int coreActiveTicks,
            int commandVisualTicks
    ) {
        synced = true;
        ClientSaeedHudState.fireAmmo = Math.max(0, fireAmmo);
        ClientSaeedHudState.fireMaxAmmo = Math.max(0, fireMaxAmmo);
        ClientSaeedHudState.fireRechargeTicks = Math.max(0, fireRechargeTicks);
        ClientSaeedHudState.crossbowEquipped = crossbowEquipped;
        ClientSaeedHudState.fireBounce = fireBounce;
        ClientSaeedHudState.rollCooldownTicks = Math.max(0, rollCooldownTicks);
        ClientSaeedHudState.coreCooldownTicks = Math.max(0, coreCooldownTicks);
        ClientSaeedHudState.coreActiveTicks = Math.max(0, coreActiveTicks);
        ClientSaeedHudState.commandVisualTicks = Math.max(ClientSaeedHudState.commandVisualTicks,
                Math.max(0, commandVisualTicks));
    }

    public static void tick() {
        if (fireRechargeTicks > 0) fireRechargeTicks--;
        if (rollCooldownTicks > 0) rollCooldownTicks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (coreActiveTicks > 0) coreActiveTicks--;
        if (commandVisualTicks > 0) commandVisualTicks--;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.SAEED_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.SAEED_ID);
    }

    public static boolean hasCrossbowEquipped() {
        return shouldRender() && crossbowEquipped;
    }

    public static int fireAmmo() {
        return fireAmmo;
    }

    public static int fireMaxAmmo() {
        return fireMaxAmmo;
    }

    public static int fireRechargeTicks() {
        return fireRechargeTicks;
    }

    public static boolean fireBounce() {
        return fireBounce;
    }

    public static int rollCooldownTicks() {
        return rollCooldownTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int coreActiveTicks() {
        return coreActiveTicks;
    }

    public static int commandVisualTicks() {
        return commandVisualTicks;
    }
}
