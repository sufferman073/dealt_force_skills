package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawTool;
import com.rzy.dealt_force_skills.client.visual.HackclawPathLineRenderer;

public final class ClientHackclawHudState {
    private static boolean synced;
    private static int knifeCharges;
    private static int knifeMaxCharges;
    private static int knifeRechargeTicks;
    private static int flashDroneCharges;
    private static int flashDroneMaxCharges;
    private static int flashDroneRechargeTicks;
    private static int coreCooldownTicks;
    private static int coreChannelTicks;
    private static int coreActiveTicks;
    private static int coreRound;
    private static boolean coreScanFound;
    private static HackclawTool equippedTool = HackclawTool.NONE;

    private ClientHackclawHudState() {
    }

    public static void reset() {
        synced = false;
        knifeCharges = 0;
        knifeMaxCharges = 0;
        knifeRechargeTicks = 0;
        flashDroneCharges = 0;
        flashDroneMaxCharges = 0;
        flashDroneRechargeTicks = 0;
        coreCooldownTicks = 0;
        coreChannelTicks = 0;
        coreActiveTicks = 0;
        coreRound = 0;
        coreScanFound = false;
        equippedTool = HackclawTool.NONE;
        HackclawPathLineRenderer.clear();
    }

    public static void sync(
            int knifeCharges,
            int knifeMaxCharges,
            int knifeRechargeTicks,
            int flashDroneCharges,
            int flashDroneMaxCharges,
            int flashDroneRechargeTicks,
            int coreCooldownTicks,
            int coreChannelTicks,
            int coreActiveTicks,
            int coreRound,
            boolean coreScanFound,
            int equippedToolOrdinal
    ) {
        synced = true;
        ClientHackclawHudState.knifeCharges = knifeCharges;
        ClientHackclawHudState.knifeMaxCharges = knifeMaxCharges;
        ClientHackclawHudState.knifeRechargeTicks = knifeRechargeTicks;
        ClientHackclawHudState.flashDroneCharges = flashDroneCharges;
        ClientHackclawHudState.flashDroneMaxCharges = flashDroneMaxCharges;
        ClientHackclawHudState.flashDroneRechargeTicks = flashDroneRechargeTicks;
        ClientHackclawHudState.coreCooldownTicks = coreCooldownTicks;
        ClientHackclawHudState.coreChannelTicks = coreChannelTicks;
        ClientHackclawHudState.coreActiveTicks = coreActiveTicks;
        ClientHackclawHudState.coreRound = coreRound;
        ClientHackclawHudState.coreScanFound = coreScanFound;
        HackclawTool[] tools = HackclawTool.values();
        ClientHackclawHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : HackclawTool.NONE;
    }

    public static void tick() {
        if (knifeRechargeTicks > 0) {
            knifeRechargeTicks--;
        }
        if (flashDroneRechargeTicks > 0) {
            flashDroneRechargeTicks--;
        }
        if (coreCooldownTicks > 0) {
            coreCooldownTicks--;
        }
        if (coreChannelTicks > 0) {
            coreChannelTicks--;
        }
        if (coreActiveTicks > 0) {
            coreActiveTicks--;
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.HACKCLAW_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != HackclawTool.NONE;
    }

    public static int knifeCharges() {
        return knifeCharges;
    }

    public static int knifeMaxCharges() {
        return knifeMaxCharges;
    }

    public static int knifeRechargeTicks() {
        return knifeRechargeTicks;
    }

    public static int flashDroneCharges() {
        return flashDroneCharges;
    }

    public static int flashDroneMaxCharges() {
        return flashDroneMaxCharges;
    }

    public static int flashDroneRechargeTicks() {
        return flashDroneRechargeTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int coreChannelTicks() {
        return coreChannelTicks;
    }

    public static int coreActiveTicks() {
        return coreActiveTicks;
    }

    public static int coreRound() {
        return coreRound;
    }

    public static boolean coreScanFound() {
        return coreScanFound;
    }

    public static HackclawTool equippedTool() {
        return equippedTool;
    }
}
