package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.uluru.UluruTool;

public final class ClientUluruHudState {
    private static boolean synced;
    private static int incendiaryCharges;
    private static int incendiaryMaxCharges;
    private static int incendiaryRechargeTicks;
    private static int coverCharges;
    private static int coverMaxCharges;
    private static int coverRechargeTicks;
    private static int missileCooldownTicks;
    private static UluruTool equippedTool = UluruTool.NONE;
    private static boolean coverPerpendicular;
    private static boolean missileAiming;

    private ClientUluruHudState() {
    }

    public static void reset() {
        synced = false;
        incendiaryCharges = 0;
        incendiaryMaxCharges = 0;
        incendiaryRechargeTicks = 0;
        coverCharges = 0;
        coverMaxCharges = 0;
        coverRechargeTicks = 0;
        missileCooldownTicks = 0;
        equippedTool = UluruTool.NONE;
        coverPerpendicular = false;
        missileAiming = false;
    }

    public static void sync(
            int incendiaryCharges,
            int incendiaryMaxCharges,
            int incendiaryRechargeTicks,
            int coverCharges,
            int coverMaxCharges,
            int coverRechargeTicks,
            int missileCooldownTicks,
            int equippedToolOrdinal,
            boolean coverPerpendicular
    ) {
        synced = true;
        ClientUluruHudState.incendiaryCharges = incendiaryCharges;
        ClientUluruHudState.incendiaryMaxCharges = incendiaryMaxCharges;
        ClientUluruHudState.incendiaryRechargeTicks = incendiaryRechargeTicks;
        ClientUluruHudState.coverCharges = coverCharges;
        ClientUluruHudState.coverMaxCharges = coverMaxCharges;
        ClientUluruHudState.coverRechargeTicks = coverRechargeTicks;
        ClientUluruHudState.missileCooldownTicks = missileCooldownTicks;
        UluruTool[] tools = UluruTool.values();
        ClientUluruHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : UluruTool.NONE;
        ClientUluruHudState.coverPerpendicular = coverPerpendicular;
        if (ClientUluruHudState.equippedTool != UluruTool.MISSILE) {
            missileAiming = false;
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.ULURU_ID);
    }

    public static void tick() {
        if (incendiaryRechargeTicks > 0) {
            incendiaryRechargeTicks--;
        }
        if (coverRechargeTicks > 0) {
            coverRechargeTicks--;
        }
        if (missileCooldownTicks > 0) {
            missileCooldownTicks--;
        }
    }

    public static int incendiaryCharges() {
        return incendiaryCharges;
    }

    public static int incendiaryMaxCharges() {
        return incendiaryMaxCharges;
    }

    public static int incendiaryRechargeTicks() {
        return incendiaryRechargeTicks;
    }

    public static int coverCharges() {
        return coverCharges;
    }

    public static int coverMaxCharges() {
        return coverMaxCharges;
    }

    public static int coverRechargeTicks() {
        return coverRechargeTicks;
    }

    public static int missileCooldownTicks() {
        return missileCooldownTicks;
    }

    public static UluruTool equippedTool() {
        return equippedTool;
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != UluruTool.NONE;
    }

    public static boolean coverPerpendicular() {
        return coverPerpendicular;
    }

    public static boolean missileAiming() {
        return missileAiming;
    }

    public static void setMissileAiming(boolean aiming) {
        missileAiming = shouldRender() && equippedTool == UluruTool.MISSILE && aiming;
    }
}
