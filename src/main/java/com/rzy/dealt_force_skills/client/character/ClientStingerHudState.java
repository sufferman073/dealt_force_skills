package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.stinger.StingerDownedMarker;
import com.rzy.dealt_force_skills.character.stinger.StingerStimMode;
import com.rzy.dealt_force_skills.character.stinger.StingerTool;

import java.util.List;

public final class ClientStingerHudState {
    private static boolean synced;
    private static int smokeCooldownTicks;
    private static int droneCooldownTicks;
    private static int stimCharges;
    private static int stimMaxCharges;
    private static int stimRechargeTicks;
    private static StingerTool equippedTool = StingerTool.NONE;
    private static StingerStimMode stimMode = StingerStimMode.HEAL;
    private static int rescueTicks;
    private static int rescueRequiredTicks;
    private static int selfDownedTicks;
    private static int stimLockTargetCount;
    private static int stimLockTicks;
    private static List<StingerDownedMarker> downedMarkers = List.of();

    private ClientStingerHudState() {
    }

    public static void reset() {
        synced = false;
        smokeCooldownTicks = 0;
        droneCooldownTicks = 0;
        stimCharges = 0;
        stimMaxCharges = 0;
        stimRechargeTicks = 0;
        equippedTool = StingerTool.NONE;
        stimMode = StingerStimMode.HEAL;
        rescueTicks = 0;
        rescueRequiredTicks = 0;
        selfDownedTicks = 0;
        stimLockTargetCount = 0;
        stimLockTicks = 0;
        downedMarkers = List.of();
    }

    public static void sync(
            int smokeCooldownTicks,
            int droneCooldownTicks,
            int stimCharges,
            int stimMaxCharges,
            int stimRechargeTicks,
            int equippedToolOrdinal,
            int stimModeOrdinal,
            int rescueTicks,
            int rescueRequiredTicks,
            int selfDownedTicks,
            List<StingerDownedMarker> downedMarkers
    ) {
        synced = true;
        ClientStingerHudState.smokeCooldownTicks = smokeCooldownTicks;
        ClientStingerHudState.droneCooldownTicks = droneCooldownTicks;
        ClientStingerHudState.stimCharges = stimCharges;
        ClientStingerHudState.stimMaxCharges = stimMaxCharges;
        ClientStingerHudState.stimRechargeTicks = stimRechargeTicks;
        StingerTool[] tools = StingerTool.values();
        ClientStingerHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : StingerTool.NONE;
        StingerStimMode[] modes = StingerStimMode.values();
        ClientStingerHudState.stimMode = stimModeOrdinal >= 0 && stimModeOrdinal < modes.length
                ? modes[stimModeOrdinal]
                : StingerStimMode.HEAL;
        ClientStingerHudState.rescueTicks = rescueTicks;
        ClientStingerHudState.rescueRequiredTicks = rescueRequiredTicks;
        ClientStingerHudState.selfDownedTicks = selfDownedTicks;
        ClientStingerHudState.downedMarkers = List.copyOf(downedMarkers);
    }

    public static void tick() {
        if (smokeCooldownTicks > 0) {
            smokeCooldownTicks--;
        }
        if (droneCooldownTicks > 0) {
            droneCooldownTicks--;
        }
        if (stimRechargeTicks > 0) {
            stimRechargeTicks--;
        }
        if (rescueTicks > 0 && rescueRequiredTicks <= 0) {
            rescueTicks--;
        }
        if (selfDownedTicks > 0) {
            selfDownedTicks--;
        }
        if (stimLockTicks > 0) {
            stimLockTicks--;
        } else {
            stimLockTargetCount = 0;
        }
    }

    public static void syncStimLockTargets(int targetCount, int durationTicks) {
        stimLockTargetCount = Math.max(0, targetCount);
        stimLockTicks = stimLockTargetCount > 0 ? Math.max(1, durationTicks) : 0;
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.STINGER_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != StingerTool.NONE;
    }

    public static int smokeCooldownTicks() {
        return smokeCooldownTicks;
    }

    public static int droneCooldownTicks() {
        return droneCooldownTicks;
    }

    public static int stimCharges() {
        return stimCharges;
    }

    public static int stimMaxCharges() {
        return stimMaxCharges;
    }

    public static int stimRechargeTicks() {
        return stimRechargeTicks;
    }

    public static StingerTool equippedTool() {
        return equippedTool;
    }

    public static StingerStimMode stimMode() {
        return stimMode;
    }

    public static int rescueTicks() {
        return rescueTicks;
    }

    public static int rescueRequiredTicks() {
        return rescueRequiredTicks;
    }

    public static int selfDownedTicks() {
        return selfDownedTicks;
    }

    public static int stimLockTargetCount() {
        return stimLockTicks > 0 ? stimLockTargetCount : 0;
    }

    public static List<StingerDownedMarker> downedMarkers() {
        return downedMarkers;
    }
}
