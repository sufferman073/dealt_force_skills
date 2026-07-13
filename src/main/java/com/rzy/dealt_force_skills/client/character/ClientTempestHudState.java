package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.tempest.TempestTool;

public final class ClientTempestHudState {
    private static boolean synced;
    private static int wallCharges;
    private static int wallMaxCharges;
    private static int wallRechargeTicks;
    private static int rollCooldownTicks;
    private static int coreCooldownTicks;
    private static TempestTool equippedTool = TempestTool.NONE;
    private static boolean ropeActive;
    private static double ropeRemaining;
    private static boolean recalling;
    private static boolean pendingLandingRoll;
    private static int downedTicks;
    private static int selfRescueTicks;
    private static int selfRescueRequiredTicks;

    private ClientTempestHudState() {
    }

    public static void reset() {
        synced = false;
        wallCharges = 0;
        wallMaxCharges = 0;
        wallRechargeTicks = 0;
        rollCooldownTicks = 0;
        coreCooldownTicks = 0;
        equippedTool = TempestTool.NONE;
        ropeActive = false;
        ropeRemaining = 0.0D;
        recalling = false;
        pendingLandingRoll = false;
        downedTicks = 0;
        selfRescueTicks = 0;
        selfRescueRequiredTicks = 0;
    }

    public static void sync(
            int wallCharges,
            int wallMaxCharges,
            int wallRechargeTicks,
            int rollCooldownTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal,
            boolean ropeActive,
            double ropeRemaining,
            boolean recalling,
            boolean pendingLandingRoll,
            int downedTicks,
            int selfRescueTicks,
            int selfRescueRequiredTicks
    ) {
        synced = true;
        ClientTempestHudState.wallCharges = wallCharges;
        ClientTempestHudState.wallMaxCharges = wallMaxCharges;
        ClientTempestHudState.wallRechargeTicks = wallRechargeTicks;
        ClientTempestHudState.rollCooldownTicks = rollCooldownTicks;
        ClientTempestHudState.coreCooldownTicks = coreCooldownTicks;
        TempestTool[] tools = TempestTool.values();
        ClientTempestHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : TempestTool.NONE;
        ClientTempestHudState.ropeActive = ropeActive;
        ClientTempestHudState.ropeRemaining = Math.max(0.0D, ropeRemaining);
        ClientTempestHudState.recalling = recalling;
        ClientTempestHudState.pendingLandingRoll = pendingLandingRoll;
        ClientTempestHudState.downedTicks = downedTicks;
        ClientTempestHudState.selfRescueTicks = selfRescueTicks;
        ClientTempestHudState.selfRescueRequiredTicks = selfRescueRequiredTicks;
    }

    public static void tick() {
        if (wallRechargeTicks > 0) wallRechargeTicks--;
        if (rollCooldownTicks > 0) rollCooldownTicks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (downedTicks > 0) downedTicks--;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.TEMPEST_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.TEMPEST_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != TempestTool.NONE;
    }

    public static boolean actionLocked() {
        return shouldRender() && (recalling || downedTicks > 0);
    }

    public static int wallCharges() { return wallCharges; }
    public static int wallMaxCharges() { return wallMaxCharges; }
    public static int wallRechargeTicks() { return wallRechargeTicks; }
    public static int rollCooldownTicks() { return rollCooldownTicks; }
    public static int coreCooldownTicks() { return coreCooldownTicks; }
    public static TempestTool equippedTool() { return equippedTool; }
    public static boolean ropeActive() { return ropeActive; }
    public static double ropeRemaining() { return ropeRemaining; }
    public static boolean recalling() { return recalling; }
    public static boolean pendingLandingRoll() { return pendingLandingRoll; }
    public static int downedTicks() { return downedTicks; }
    public static int selfRescueTicks() { return selfRescueTicks; }
    public static int selfRescueRequiredTicks() { return selfRescueRequiredTicks; }
}
