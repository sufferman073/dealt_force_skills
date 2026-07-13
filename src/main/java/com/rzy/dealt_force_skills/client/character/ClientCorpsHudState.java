package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;

public final class ClientCorpsHudState {
    private static boolean synced;
    private static int active1CooldownTicks;
    private static int active2CooldownTicks;
    private static int coreCooldownTicks;
    private static int duelRemainingTicks;
    private static int loyaltyOrangeStacks;
    private static boolean duelTargetMarkerActive;
    private static String duelTargetMarkerDimension = "";
    private static double duelTargetMarkerX;
    private static double duelTargetMarkerY;
    private static double duelTargetMarkerZ;

    private ClientCorpsHudState() {
    }

    public static void reset() {
        synced = false;
        active1CooldownTicks = 0;
        active2CooldownTicks = 0;
        coreCooldownTicks = 0;
        duelRemainingTicks = 0;
        loyaltyOrangeStacks = 0;
        duelTargetMarkerActive = false;
        duelTargetMarkerDimension = "";
        duelTargetMarkerX = 0.0D;
        duelTargetMarkerY = 0.0D;
        duelTargetMarkerZ = 0.0D;
    }

    public static void sync(int active1CooldownTicks, int active2CooldownTicks, int coreCooldownTicks,
                            int duelRemainingTicks, int loyaltyOrangeStacks,
                            boolean duelTargetMarkerActive, String duelTargetMarkerDimension,
                            double duelTargetMarkerX, double duelTargetMarkerY, double duelTargetMarkerZ) {
        synced = true;
        ClientCorpsHudState.active1CooldownTicks = active1CooldownTicks;
        ClientCorpsHudState.active2CooldownTicks = active2CooldownTicks;
        ClientCorpsHudState.coreCooldownTicks = coreCooldownTicks;
        ClientCorpsHudState.duelRemainingTicks = duelRemainingTicks;
        ClientCorpsHudState.loyaltyOrangeStacks = loyaltyOrangeStacks;
        ClientCorpsHudState.duelTargetMarkerActive = duelTargetMarkerActive && duelRemainingTicks > 0;
        ClientCorpsHudState.duelTargetMarkerDimension = duelTargetMarkerDimension == null ? "" : duelTargetMarkerDimension;
        ClientCorpsHudState.duelTargetMarkerX = duelTargetMarkerX;
        ClientCorpsHudState.duelTargetMarkerY = duelTargetMarkerY;
        ClientCorpsHudState.duelTargetMarkerZ = duelTargetMarkerZ;
    }

    public static void tick() {
        if (active1CooldownTicks > 0) active1CooldownTicks--;
        if (active2CooldownTicks > 0) active2CooldownTicks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (duelRemainingTicks > 0) duelRemainingTicks--;
        if (duelRemainingTicks <= 0) {
            duelTargetMarkerActive = false;
        }
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.CORPS_ID);
    }

    public static int active1CooldownTicks() {
        return active1CooldownTicks;
    }

    public static int active2CooldownTicks() {
        return active2CooldownTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int duelRemainingTicks() {
        return duelRemainingTicks;
    }

    public static int loyaltyOrangeStacks() {
        return loyaltyOrangeStacks;
    }

    public static boolean hasDuelTargetMarker() {
        return shouldDisplay() && duelTargetMarkerActive && duelRemainingTicks > 0;
    }

    public static String duelTargetMarkerDimension() {
        return duelTargetMarkerDimension;
    }

    public static double duelTargetMarkerX() {
        return duelTargetMarkerX;
    }

    public static double duelTargetMarkerY() {
        return duelTargetMarkerY;
    }

    public static double duelTargetMarkerZ() {
        return duelTargetMarkerZ;
    }
}
