package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.vlinder.VlinderDroneMode;
import com.rzy.dealt_force_skills.character.vlinder.VlinderTool;
import com.rzy.dealt_force_skills.character.vlinder.VlinderWorldMarker;

import java.util.List;

public final class ClientVlinderHudState {
    private static boolean synced;
    private static int medicalCharges;
    private static int medicalMaxCharges;
    private static int medicalRechargeTicks;
    private static int smokeCharges;
    private static int smokeMaxCharges;
    private static int smokeRechargeTicks;
    private static int coreCooldownTicks;
    private static VlinderTool equippedTool = VlinderTool.NONE;
    private static VlinderDroneMode droneMode = VlinderDroneMode.HEAL;
    private static int lockedTargetId = -1;
    private static int rescueTicks;
    private static int rescueRequiredTicks;
    private static int selfDownedTicks;
    private static int rescueProtectionTicks;
    private static int activeDefenseTicks;
    private static int injectionTicks;
    private static int injectionRequiredTicks;
    private static List<VlinderWorldMarker> markers = List.of();

    private ClientVlinderHudState() {
    }

    public static void reset() {
        synced = false;
        medicalCharges = 0;
        medicalMaxCharges = 0;
        medicalRechargeTicks = 0;
        smokeCharges = 0;
        smokeMaxCharges = 0;
        smokeRechargeTicks = 0;
        coreCooldownTicks = 0;
        equippedTool = VlinderTool.NONE;
        droneMode = VlinderDroneMode.HEAL;
        lockedTargetId = -1;
        rescueTicks = 0;
        rescueRequiredTicks = 0;
        selfDownedTicks = 0;
        rescueProtectionTicks = 0;
        activeDefenseTicks = 0;
        injectionTicks = 0;
        injectionRequiredTicks = 0;
        markers = List.of();
    }

    public static void sync(
            int medicalCharges,
            int medicalMaxCharges,
            int medicalRechargeTicks,
            int smokeCharges,
            int smokeMaxCharges,
            int smokeRechargeTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal,
            int droneModeOrdinal,
            int lockedTargetId,
            int rescueTicks,
            int rescueRequiredTicks,
            int selfDownedTicks,
            int rescueProtectionTicks,
            int activeDefenseTicks,
            int injectionTicks,
            int injectionRequiredTicks,
            List<VlinderWorldMarker> markers
    ) {
        synced = true;
        ClientVlinderHudState.medicalCharges = medicalCharges;
        ClientVlinderHudState.medicalMaxCharges = medicalMaxCharges;
        ClientVlinderHudState.medicalRechargeTicks = medicalRechargeTicks;
        ClientVlinderHudState.smokeCharges = smokeCharges;
        ClientVlinderHudState.smokeMaxCharges = smokeMaxCharges;
        ClientVlinderHudState.smokeRechargeTicks = smokeRechargeTicks;
        ClientVlinderHudState.coreCooldownTicks = coreCooldownTicks;
        VlinderTool[] tools = VlinderTool.values();
        ClientVlinderHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : VlinderTool.NONE;
        VlinderDroneMode[] modes = VlinderDroneMode.values();
        ClientVlinderHudState.droneMode = droneModeOrdinal >= 0 && droneModeOrdinal < modes.length
                ? modes[droneModeOrdinal]
                : VlinderDroneMode.HEAL;
        ClientVlinderHudState.lockedTargetId = lockedTargetId;
        ClientVlinderHudState.rescueTicks = rescueTicks;
        ClientVlinderHudState.rescueRequiredTicks = rescueRequiredTicks;
        ClientVlinderHudState.selfDownedTicks = selfDownedTicks;
        ClientVlinderHudState.rescueProtectionTicks = rescueProtectionTicks;
        ClientVlinderHudState.activeDefenseTicks = activeDefenseTicks;
        ClientVlinderHudState.injectionTicks = injectionTicks;
        ClientVlinderHudState.injectionRequiredTicks = injectionRequiredTicks;
        ClientVlinderHudState.markers = List.copyOf(markers);
    }

    public static void tick() {
        if (medicalRechargeTicks > 0) medicalRechargeTicks--;
        if (smokeRechargeTicks > 0) smokeRechargeTicks--;
        if (coreCooldownTicks > 0) coreCooldownTicks--;
        if (selfDownedTicks > 0) selfDownedTicks--;
        if (rescueProtectionTicks > 0) rescueProtectionTicks--;
        if (activeDefenseTicks > 0) activeDefenseTicks--;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.VLINDER_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.VLINDER_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != VlinderTool.NONE;
    }

    public static int medicalCharges() { return medicalCharges; }
    public static int medicalMaxCharges() { return medicalMaxCharges; }
    public static int medicalRechargeTicks() { return medicalRechargeTicks; }
    public static int smokeCharges() { return smokeCharges; }
    public static int smokeMaxCharges() { return smokeMaxCharges; }
    public static int smokeRechargeTicks() { return smokeRechargeTicks; }
    public static int coreCooldownTicks() { return coreCooldownTicks; }
    public static VlinderTool equippedTool() { return equippedTool; }
    public static VlinderDroneMode droneMode() { return droneMode; }
    public static int lockedTargetId() { return lockedTargetId; }
    public static int rescueTicks() { return rescueTicks; }
    public static int rescueRequiredTicks() { return rescueRequiredTicks; }
    public static int selfDownedTicks() { return selfDownedTicks; }
    public static int rescueProtectionTicks() { return rescueProtectionTicks; }
    public static int activeDefenseTicks() { return activeDefenseTicks; }
    public static int injectionTicks() { return injectionTicks; }
    public static int injectionRequiredTicks() { return injectionRequiredTicks; }
    public static List<VlinderWorldMarker> markers() { return markers; }
}
