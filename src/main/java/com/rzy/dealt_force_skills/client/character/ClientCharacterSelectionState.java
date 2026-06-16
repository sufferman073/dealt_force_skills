package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.client.screen.CharacterSelectionScreen;
import com.rzy.dealt_force_skills.network.C2S_SelectCharacter;
import com.rzy.dealt_force_skills.network.C2S_UseCharacterSkill;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.client.RaptorFalconController;
import com.rzy.dealt_force_skills.client.UluruMissileController;
import net.minecraft.client.Minecraft;

public final class ClientCharacterSelectionState {
    private static String selectedCharacterId;
    private static boolean receivedServerState;
    private static boolean promptedForCurrentWorld;

    private ClientCharacterSelectionState() {
    }

    public static boolean hasSelectedCharacter() {
        return selectedCharacterId != null && !selectedCharacterId.isBlank();
    }

    public static String selectedCharacterId() {
        return hasSelectedCharacter() ? selectedCharacterId : "";
    }

    public static boolean isSelectedCharacter(String characterId) {
        return hasSelectedCharacter() && selectedCharacterId.equals(characterId);
    }

    public static void resetSession() {
        selectedCharacterId = null;
        receivedServerState = false;
        promptedForCurrentWorld = false;
        ClientCharacterSkinState.clear();
        ClientSinevaHudState.reset();
        ClientUluruHudState.reset();
        ClientDWolfHudState.reset();
        ClientGizmoHudState.reset();
        ClientShepherdHudState.reset();
        ClientLunaHudState.reset();
        ClientHackclawHudState.reset();
        ClientVyronHudState.reset();
        ClientStingerHudState.reset();
        ClientNoxHudState.reset();
        ClientManbaHudState.reset();
        ClientNikaidouHiroHudState.reset();
        ClientCatDadHudState.reset();
        ClientDepartmentHudState.reset();
        ClientUndeadHudState.reset();
        ClientLexNinjiaHudState.reset();
        ClientMorseHudState.reset();
        ClientToxikHudState.reset();
        ClientRaptorHudState.reset();
        ClientVlinderHudState.reset();
        ClientTempestHudState.reset();
    }

    public static void syncSelectedCharacter(String characterId) {
        receivedServerState = true;
        selectedCharacterId = characterId == null || characterId.isBlank() ? null : characterId;
        ClientCharacterSkinState.syncLocalSelectedCharacter(selectedCharacterId);
        if (!isSelectedCharacter(ModCharacters.SINEVA_ID)) {
            ClientSinevaHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.ULURU_ID)) {
            ClientUluruHudState.reset();
            UluruMissileController.stop(false);
        }
        if (!isSelectedCharacter(ModCharacters.D_WOLF_ID)) {
            ClientDWolfHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.GIZMO_ID)) {
            ClientGizmoHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.SHEPHERD_ID)) {
            ClientShepherdHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.LUNA_ID)) {
            ClientLunaHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.HACKCLAW_ID)) {
            ClientHackclawHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.VYRON_ID)) {
            ClientVyronHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.STINGER_ID)) {
            ClientStingerHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.NOX_ID)) {
            ClientNoxHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.MANBA_ID)) {
            ClientManbaHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.NIKAIDOU_HIRO_ID)) {
            ClientNikaidouHiroHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.CATDAD_ID)) {
            ClientCatDadHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.DEPARTMENT_OF_TRANSPORTATION_ID)) {
            ClientDepartmentHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.UNDEAD_ID)) {
            ClientUndeadHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.LEX_NINJIA_ID)) {
            ClientLexNinjiaHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.MORSE_ID)) {
            ClientMorseHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.TOXIK_ID)) {
            ClientToxikHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.RAPTOR_ID)) {
            ClientRaptorHudState.reset();
            RaptorFalconController.stop(false);
        }
        if (!isSelectedCharacter(ModCharacters.VLINDER_ID)) {
            ClientVlinderHudState.reset();
        }
        if (!isSelectedCharacter(ModCharacters.TEMPEST_ID)) {
            ClientTempestHudState.reset();
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (hasSelectedCharacter() && minecraft.screen instanceof CharacterSelectionScreen) {
            minecraft.setScreen(null);
        }
    }

    public static void openInitialSelectionIfNeeded() {
        Minecraft minecraft = Minecraft.getInstance();
        if (!receivedServerState || hasSelectedCharacter() || promptedForCurrentWorld || minecraft.screen != null) {
            return;
        }
        openSelectionScreen();
    }

    public static void openSelectionScreen() {
        openSelectionScreen(false);
    }

    public static void openReselectionScreen() {
        openSelectionScreen(true);
    }

    private static void openSelectionScreen(boolean allowReselection) {
        if (UluruMissileController.isControlling() || RaptorFalconController.isControlling()) {
            return;
        }

        if (!receivedServerState
                || (hasSelectedCharacter() && !allowReselection && !canReselectInCurrentMode())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof CharacterSelectionScreen) {
            return;
        }
        promptedForCurrentWorld = true;
        minecraft.setScreen(new CharacterSelectionScreen());
    }

    public static void selectCharacter(String characterId) {
        if (UluruMissileController.isControlling() || RaptorFalconController.isControlling()) {
            return;
        }

        promptedForCurrentWorld = true;
        NetworkHandler.sendToServer(new C2S_SelectCharacter(characterId));
        Minecraft.getInstance().setScreen(null);
    }

    public static boolean canReselectInCurrentMode() {
        Minecraft minecraft = Minecraft.getInstance();
        return minecraft.player != null && minecraft.player.getAbilities().instabuild;
    }

    public static void useSkill(SkillSlot slot) {
        useSkill(slot, false);
    }

    public static void useSkill(SkillSlot slot, boolean alternate) {
        if (!receivedServerState || UluruMissileController.isControlling() || RaptorFalconController.isControlling()) {
            return;
        }

        if (!hasSelectedCharacter()) {
            openSelectionScreen();
            return;
        }
        NetworkHandler.sendToServer(new C2S_UseCharacterSkill(slot, alternate));
    }
}
