package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.department.DepartmentTool;
import com.rzy.dealt_force_skills.character.department.DepartmentTrapMarker;

import java.util.List;

public final class ClientDepartmentHudState {
    private static boolean synced;
    private static int laserCooldownTicks;
    private static int trapCooldownTicks;
    private static int coreCooldownTicks;
    private static int coreCountdownTicks;
    private static int coreAscendTicks;
    private static DepartmentTool equippedTool = DepartmentTool.NONE;
    private static int reductionSteps;
    private static int vulnerabilityStacks;
    private static boolean concealed;
    private static int calibrationSuccesses;
    private static int calibrationWindow = 350;
    private static int calibrationWindowStart;
    private static int trapCharges;
    private static List<DepartmentTrapMarker> trapMarkers = List.of();

    private ClientDepartmentHudState() {
    }

    public static void reset() {
        synced = false;
        laserCooldownTicks = 0;
        trapCooldownTicks = 0;
        coreCooldownTicks = 0;
        coreCountdownTicks = 0;
        coreAscendTicks = 0;
        equippedTool = DepartmentTool.NONE;
        reductionSteps = 0;
        vulnerabilityStacks = 0;
        concealed = false;
        calibrationSuccesses = 0;
        calibrationWindow = 350;
        calibrationWindowStart = 0;
        trapCharges = 0;
        trapMarkers = List.of();
    }

    public static void sync(
            int laserCooldownTicks,
            int trapCooldownTicks,
            int coreCooldownTicks,
            int coreCountdownTicks,
            int coreAscendTicks,
            int equippedToolOrdinal,
            int reductionSteps,
            int vulnerabilityStacks,
            boolean concealed,
            int calibrationSuccesses,
            int calibrationWindow,
            int calibrationWindowStart,
            int trapCharges,
            List<DepartmentTrapMarker> trapMarkers
    ) {
        synced = true;
        ClientDepartmentHudState.laserCooldownTicks = laserCooldownTicks;
        ClientDepartmentHudState.trapCooldownTicks = trapCooldownTicks;
        ClientDepartmentHudState.coreCooldownTicks = coreCooldownTicks;
        ClientDepartmentHudState.coreCountdownTicks = coreCountdownTicks;
        ClientDepartmentHudState.coreAscendTicks = coreAscendTicks;
        DepartmentTool[] tools = DepartmentTool.values();
        ClientDepartmentHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : DepartmentTool.NONE;
        ClientDepartmentHudState.reductionSteps = reductionSteps;
        ClientDepartmentHudState.vulnerabilityStacks = vulnerabilityStacks;
        ClientDepartmentHudState.concealed = concealed;
        ClientDepartmentHudState.calibrationSuccesses = calibrationSuccesses;
        ClientDepartmentHudState.calibrationWindow = calibrationWindow;
        ClientDepartmentHudState.calibrationWindowStart = calibrationWindowStart;
        ClientDepartmentHudState.trapCharges = trapCharges;
        ClientDepartmentHudState.trapMarkers = List.copyOf(trapMarkers);
    }

    public static void tick() {
        if (laserCooldownTicks > 0) {
            laserCooldownTicks--;
        }
        if (trapCooldownTicks > 0) {
            trapCooldownTicks--;
        }
        if (coreCooldownTicks > 0) {
            coreCooldownTicks--;
        }
        if (coreCountdownTicks > 0) {
            coreCountdownTicks--;
        }
        if (coreAscendTicks > 0) {
            coreAscendTicks--;
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.DEPARTMENT_OF_TRANSPORTATION_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != DepartmentTool.NONE;
    }

    public static int laserCooldownTicks() {
        return laserCooldownTicks;
    }

    public static int trapCooldownTicks() {
        return trapCooldownTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static int coreCountdownTicks() {
        return coreCountdownTicks;
    }

    public static int coreAscendTicks() {
        return coreAscendTicks;
    }

    public static DepartmentTool equippedTool() {
        return equippedTool;
    }

    public static int reductionSteps() {
        return reductionSteps;
    }

    public static int vulnerabilityStacks() {
        return vulnerabilityStacks;
    }

    public static boolean concealed() {
        return concealed;
    }

    public static int calibrationSuccesses() {
        return calibrationSuccesses;
    }

    public static int calibrationWindow() {
        return calibrationWindow;
    }

    public static int calibrationWindowStart() {
        return calibrationWindowStart;
    }

    public static int trapCharges() {
        return trapCharges;
    }

    public static List<DepartmentTrapMarker> trapMarkers() {
        return trapMarkers;
    }
}
