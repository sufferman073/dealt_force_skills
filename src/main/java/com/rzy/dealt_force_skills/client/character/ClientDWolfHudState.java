package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.dwolf.DWolfTool;
import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import net.minecraft.client.Minecraft;

public final class ClientDWolfHudState {
    private static boolean synced;
    private static int handCannonCharges;
    private static int handCannonMaxCharges;
    private static int handCannonRechargeTicks;
    private static int smokeCharges;
    private static int smokeMaxCharges;
    private static int smokeRechargeTicks;
    private static int overloadCooldownTicks;
    private static int overloadStartupTicks;
    private static int overloadActiveTicks;
    private static DWolfTool equippedTool = DWolfTool.NONE;
    private static int cannonBurstShots;
    private static int slideTicks;

    private ClientDWolfHudState() {
    }

    public static void reset() {
        synced = false;
        handCannonCharges = 0;
        handCannonMaxCharges = 0;
        handCannonRechargeTicks = 0;
        smokeCharges = 0;
        smokeMaxCharges = 0;
        smokeRechargeTicks = 0;
        overloadCooldownTicks = 0;
        overloadStartupTicks = 0;
        overloadActiveTicks = 0;
        equippedTool = DWolfTool.NONE;
        cannonBurstShots = 0;
        slideTicks = 0;
    }

    public static void sync(
            int handCannonCharges,
            int handCannonMaxCharges,
            int handCannonRechargeTicks,
            int smokeCharges,
            int smokeMaxCharges,
            int smokeRechargeTicks,
            int overloadCooldownTicks,
            int overloadStartupTicks,
            int overloadActiveTicks,
            int equippedToolOrdinal,
            int cannonBurstShots,
            int slideTicks
    ) {
        synced = true;
        ClientDWolfHudState.handCannonCharges = handCannonCharges;
        ClientDWolfHudState.handCannonMaxCharges = handCannonMaxCharges;
        ClientDWolfHudState.handCannonRechargeTicks = handCannonRechargeTicks;
        ClientDWolfHudState.smokeCharges = smokeCharges;
        ClientDWolfHudState.smokeMaxCharges = smokeMaxCharges;
        ClientDWolfHudState.smokeRechargeTicks = smokeRechargeTicks;
        ClientDWolfHudState.overloadCooldownTicks = overloadCooldownTicks;
        ClientDWolfHudState.overloadStartupTicks = overloadStartupTicks;
        ClientDWolfHudState.overloadActiveTicks = overloadActiveTicks;
        DWolfTool[] tools = DWolfTool.values();
        ClientDWolfHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : DWolfTool.NONE;
        ClientDWolfHudState.cannonBurstShots = cannonBurstShots;
        ClientDWolfHudState.slideTicks = slideTicks;
    }

    public static void tick() {
        if (handCannonRechargeTicks > 0) {
            handCannonRechargeTicks--;
        }
        if (smokeRechargeTicks > 0) {
            smokeRechargeTicks--;
        }
        if (overloadCooldownTicks > 0) {
            overloadCooldownTicks--;
        }
        if (overloadStartupTicks > 0) {
            overloadStartupTicks--;
        }
        if (overloadActiveTicks > 0) {
            overloadActiveTicks--;
        }
        if (slideTicks > 0) {
            slideTicks--;
        }
    }

    public static void restoreStaminaPercent(int percent) {
        ParcoolStaminaBridge.recoverLocalPercent(Minecraft.getInstance().player, percent);
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.D_WOLF_ID);
    }

    public static boolean hasHandCannonEquipped() {
        return shouldRender() && equippedTool == DWolfTool.HAND_CANNON;
    }

    public static int handCannonCharges() {
        return handCannonCharges;
    }

    public static int handCannonMaxCharges() {
        return handCannonMaxCharges;
    }

    public static int handCannonRechargeTicks() {
        return handCannonRechargeTicks;
    }

    public static int smokeCharges() {
        return smokeCharges;
    }

    public static int smokeMaxCharges() {
        return smokeMaxCharges;
    }

    public static int smokeRechargeTicks() {
        return smokeRechargeTicks;
    }

    public static int overloadCooldownTicks() {
        return overloadCooldownTicks;
    }

    public static int overloadStartupTicks() {
        return overloadStartupTicks;
    }

    public static int overloadActiveTicks() {
        return overloadActiveTicks;
    }

    public static DWolfTool equippedTool() {
        return equippedTool;
    }

    public static int cannonBurstShots() {
        return cannonBurstShots;
    }

    public static int slideTicks() {
        return slideTicks;
    }
}
