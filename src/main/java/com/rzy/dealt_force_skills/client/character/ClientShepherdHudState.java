package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.shepherd.ShepherdTool;
import com.rzy.dealt_force_skills.character.shepherd.ShepherdTrapMarker;

import java.util.List;

public final class ClientShepherdHudState {
    private static boolean synced;
    private static int trapCharges;
    private static int trapMaxCharges;
    private static int trapRechargeTicks;
    private static int fragCharges;
    private static int fragMaxCharges;
    private static int fragRechargeTicks;
    private static int coreCooldownTicks;
    private static ShepherdTool equippedTool = ShepherdTool.NONE;
    private static int grenadeCookTicks;
    private static List<ShepherdTrapMarker> trapMarkers = List.of();

    private ClientShepherdHudState() {
    }

    public static void reset() {
        synced = false;
        trapCharges = 0;
        trapMaxCharges = 0;
        trapRechargeTicks = 0;
        fragCharges = 0;
        fragMaxCharges = 0;
        fragRechargeTicks = 0;
        coreCooldownTicks = 0;
        equippedTool = ShepherdTool.NONE;
        grenadeCookTicks = 0;
        trapMarkers = List.of();
    }

    public static void sync(
            int trapCharges,
            int trapMaxCharges,
            int trapRechargeTicks,
            int fragCharges,
            int fragMaxCharges,
            int fragRechargeTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal,
            int grenadeCookTicks,
            List<ShepherdTrapMarker> trapMarkers
    ) {
        synced = true;
        ClientShepherdHudState.trapCharges = trapCharges;
        ClientShepherdHudState.trapMaxCharges = trapMaxCharges;
        ClientShepherdHudState.trapRechargeTicks = trapRechargeTicks;
        ClientShepherdHudState.fragCharges = fragCharges;
        ClientShepherdHudState.fragMaxCharges = fragMaxCharges;
        ClientShepherdHudState.fragRechargeTicks = fragRechargeTicks;
        ClientShepherdHudState.coreCooldownTicks = coreCooldownTicks;
        ShepherdTool[] tools = ShepherdTool.values();
        ClientShepherdHudState.equippedTool = equippedToolOrdinal >= 0 && equippedToolOrdinal < tools.length
                ? tools[equippedToolOrdinal]
                : ShepherdTool.NONE;
        ClientShepherdHudState.grenadeCookTicks = grenadeCookTicks;
        ClientShepherdHudState.trapMarkers = List.copyOf(trapMarkers);
    }

    public static void tick() {
        if (trapRechargeTicks > 0) {
            trapRechargeTicks--;
        }
        if (fragRechargeTicks > 0) {
            fragRechargeTicks--;
        }
        if (coreCooldownTicks > 0) {
            coreCooldownTicks--;
        }
        if (equippedTool == ShepherdTool.FRAG_GRENADE && grenadeCookTicks > 0) {
            grenadeCookTicks++;
        }
    }

    public static boolean shouldRender() {
        return synced && ClientCharacterSelectionState.isSelectedCharacter(ModCharacters.SHEPHERD_ID);
    }

    public static boolean shouldDisplay() {
        return synced && ClientCharacterSelectionState.isDisplayedCharacter(ModCharacters.SHEPHERD_ID);
    }

    public static boolean hasEquippedTool() {
        return shouldRender() && equippedTool != ShepherdTool.NONE;
    }

    public static int trapCharges() {
        return trapCharges;
    }

    public static int trapMaxCharges() {
        return trapMaxCharges;
    }

    public static int trapRechargeTicks() {
        return trapRechargeTicks;
    }

    public static int fragCharges() {
        return fragCharges;
    }

    public static int fragMaxCharges() {
        return fragMaxCharges;
    }

    public static int fragRechargeTicks() {
        return fragRechargeTicks;
    }

    public static int coreCooldownTicks() {
        return coreCooldownTicks;
    }

    public static ShepherdTool equippedTool() {
        return equippedTool;
    }

    public static int grenadeCookTicks() {
        return grenadeCookTicks;
    }

    public static List<ShepherdTrapMarker> trapMarkers() {
        return trapMarkers;
    }
}
