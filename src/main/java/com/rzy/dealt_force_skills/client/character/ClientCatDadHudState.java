package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;

public final class ClientCatDadHudState {
    private static boolean synced;
    private static int hissStage;
    private static int hissTimerTicks;
    private static int hissCooldownTicks;
    private static int blockCharges;
    private static int blockMaxCharges;
    private static int blockRechargeTicks;
    private static int blockWindowTicks;
    private static int coreCooldownTicks;
    private static int downedTicks;
    private static int selfRescueTicks;
    private static int selfRescueRequiredTicks;
    private static boolean truckBreakBlocks;

    private ClientCatDadHudState() {
    }

    public static void reset() {
        synced = false;
        hissStage = 0;
        hissTimerTicks = 0;
        hissCooldownTicks = 0;
        blockCharges = 0;
        blockMaxCharges = 0;
        blockRechargeTicks = 0;
        blockWindowTicks = 0;
        coreCooldownTicks = 0;
        downedTicks = 0;
        selfRescueTicks = 0;
        selfRescueRequiredTicks = 0;
        truckBreakBlocks = false;
    }

    public static void sync(int hissStage, int hissTimerTicks, int hissCooldownTicks,
                            int blockCharges, int blockMaxCharges, int blockRechargeTicks,
                            int blockWindowTicks, int coreCooldownTicks, int downedTicks,
                            int selfRescueTicks, int selfRescueRequiredTicks, boolean truckBreakBlocks) {
        synced = true;
        ClientCatDadHudState.hissStage = hissStage;
        ClientCatDadHudState.hissTimerTicks = hissTimerTicks;
        ClientCatDadHudState.hissCooldownTicks = hissCooldownTicks;
        ClientCatDadHudState.blockCharges = blockCharges;
        ClientCatDadHudState.blockMaxCharges = blockMaxCharges;
        ClientCatDadHudState.blockRechargeTicks = blockRechargeTicks;
        ClientCatDadHudState.blockWindowTicks = blockWindowTicks;
        ClientCatDadHudState.coreCooldownTicks = coreCooldownTicks;
        ClientCatDadHudState.downedTicks = downedTicks;
        ClientCatDadHudState.selfRescueTicks = selfRescueTicks;
        ClientCatDadHudState.selfRescueRequiredTicks = selfRescueRequiredTicks;
        ClientCatDadHudState.truckBreakBlocks = truckBreakBlocks;
    }

    public static void tick() {
        if (hissTimerTicks > 0) hissTimerTicks--;
        if (hissCooldownTicks > 0) hissCooldownTicks--;
        if (blockRechargeTicks > 0) blockRechargeTicks--;
        if (blockWindowTicks > 0) blockWindowTicks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (downedTicks > 0) downedTicks--;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.CATDAD_ID);
    }

    public static boolean actionLocked() {
        return shouldRender() && downedTicks > 0;
    }

    public static int hissStage() { return hissStage; }
    public static int hissTimerTicks() { return hissTimerTicks; }
    public static int hissCooldownTicks() { return hissCooldownTicks; }
    public static int blockCharges() { return blockCharges; }
    public static int blockMaxCharges() { return blockMaxCharges; }
    public static int blockRechargeTicks() { return blockRechargeTicks; }
    public static int blockWindowTicks() { return blockWindowTicks; }
    public static int coreCooldownTicks() { return coreCooldownTicks; }
    public static int downedTicks() { return downedTicks; }
    public static int selfRescueTicks() { return selfRescueTicks; }
    public static int selfRescueRequiredTicks() { return selfRescueRequiredTicks; }
    public static boolean truckBreakBlocks() { return truckBreakBlocks; }
}
