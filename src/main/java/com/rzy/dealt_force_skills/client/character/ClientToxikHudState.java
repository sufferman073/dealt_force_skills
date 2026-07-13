package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.toxik.ToxikFireflyMode;
import com.rzy.dealt_force_skills.character.toxik.ToxikTool;

public final class ClientToxikHudState {
    private static boolean synced;
    private static int adrenalineCooldownTicks;
    private static int tearGasCharges;
    private static int tearGasMaxCharges;
    private static int tearGasRechargeTicks;
    private static int fireflyCooldownTicks;
    private static ToxikTool equippedTool = ToxikTool.NONE;
    private static ToxikFireflyMode fireflyMode = ToxikFireflyMode.LETHAL;
    private static int pulloutTicks;
    private static int pulloutRequiredTicks;

    private ClientToxikHudState() {
    }

    public static void reset() {
        synced = false;
        adrenalineCooldownTicks = 0;
        tearGasCharges = 0;
        tearGasMaxCharges = 0;
        tearGasRechargeTicks = 0;
        fireflyCooldownTicks = 0;
        equippedTool = ToxikTool.NONE;
        fireflyMode = ToxikFireflyMode.LETHAL;
        pulloutTicks = 0;
        pulloutRequiredTicks = 0;
    }

    public static void sync(
            int adrenalineCooldownTicks,
            int tearGasCharges,
            int tearGasMaxCharges,
            int tearGasRechargeTicks,
            int fireflyCooldownTicks,
            int equippedToolOrdinal,
            int fireflyModeOrdinal,
            int pulloutTicks,
            int pulloutRequiredTicks
    ) {
        synced = true;
        ClientToxikHudState.adrenalineCooldownTicks = adrenalineCooldownTicks;
        ClientToxikHudState.tearGasCharges = tearGasCharges;
        ClientToxikHudState.tearGasMaxCharges = tearGasMaxCharges;
        ClientToxikHudState.tearGasRechargeTicks = tearGasRechargeTicks;
        ClientToxikHudState.fireflyCooldownTicks = fireflyCooldownTicks;
        ToxikTool[] tools = ToxikTool.values();
        ClientToxikHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : ToxikTool.NONE;
        ToxikFireflyMode[] modes = ToxikFireflyMode.values();
        ClientToxikHudState.fireflyMode = fireflyModeOrdinal >= 0 && fireflyModeOrdinal < modes.length
                ? modes[fireflyModeOrdinal]
                : ToxikFireflyMode.LETHAL;
        ClientToxikHudState.pulloutTicks = pulloutTicks;
        ClientToxikHudState.pulloutRequiredTicks = pulloutRequiredTicks;
    }

    public static void tick() {
        if (adrenalineCooldownTicks > 0) {
            adrenalineCooldownTicks--;
        }
        if (tearGasRechargeTicks > 0) {
            tearGasRechargeTicks--;
        }
        if (fireflyCooldownTicks > 0) {
            fireflyCooldownTicks--;
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.TOXIK_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.TOXIK_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != ToxikTool.NONE;
    }

    public static int adrenalineCooldownTicks() {
        return adrenalineCooldownTicks;
    }

    public static int tearGasCharges() {
        return tearGasCharges;
    }

    public static int tearGasMaxCharges() {
        return tearGasMaxCharges;
    }

    public static int tearGasRechargeTicks() {
        return tearGasRechargeTicks;
    }

    public static int fireflyCooldownTicks() {
        return fireflyCooldownTicks;
    }

    public static ToxikTool equippedTool() {
        return equippedTool;
    }

    public static ToxikFireflyMode fireflyMode() {
        return fireflyMode;
    }

    public static int pulloutTicks() {
        return pulloutTicks;
    }

    public static int pulloutRequiredTicks() {
        return pulloutRequiredTicks;
    }
}
