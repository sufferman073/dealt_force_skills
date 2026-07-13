package com.rzy.dealt_force_skills.character;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_OpenCharacterSelection;
import com.rzy.dealt_force_skills.network.S2C_SyncSelectedCharacter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class CharacterSelectionManager {
    private static final String SELECTED_CHARACTER_TAG = DealtForceSkillsMod.MODID + ".selected_character";
    private static final String CHARACTER_RESELECTION_TAG = DealtForceSkillsMod.MODID + ".character_reselection";
    private static final String NORMAL_PLAYER_TAG = DealtForceSkillsMod.MODID + ".normal_player";

    private CharacterSelectionManager() {
    }

    public static Optional<CharacterDefinition> getSelectedCharacter(Player player) {
        return getSelectedCharacterId(player).flatMap(CharacterBranchPackManager::findCharacter);
    }

    public static boolean hasSelectedCharacter(Player player) {
        return getSelectedCharacterId(player).isPresent();
    }

    public static Optional<String> getSelectedCharacterId(Player player) {
        String id = player.getPersistentData().getString(SELECTED_CHARACTER_TAG);
        if (id.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(id);
    }

    public static boolean hasCharacterReselection(Player player) {
        return player.getPersistentData().getBoolean(CHARACTER_RESELECTION_TAG);
    }

    public static boolean isNormalPlayer(Player player) {
        return player.getPersistentData().getBoolean(NORMAL_PLAYER_TAG) && !hasSelectedCharacter(player);
    }

    public static void grantCharacterReselection(ServerPlayer player) {
        player.getPersistentData().putBoolean(CHARACTER_RESELECTION_TAG, true);
    }

    public static void consumeCharacterReselection(ServerPlayer player) {
        player.getPersistentData().remove(CHARACTER_RESELECTION_TAG);
    }

    public static void clearNormalPlayer(ServerPlayer player) {
        player.getPersistentData().remove(NORMAL_PLAYER_TAG);
        NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(getSelectedCharacterId(player).orElse(""), false), player);
    }

    public static Optional<CharacterDefinition> selectCharacter(ServerPlayer player, String characterId) {
        if (hasSelectedCharacter(player)) {
            return Optional.empty();
        }

        return CharacterBranchPackManager.findCharacter(characterId).map(character -> {
            player.getPersistentData().remove(NORMAL_PLAYER_TAG);
            player.getPersistentData().putString(SELECTED_CHARACTER_TAG, character.id());
            CharacterSkinSync.syncToTracking(player);
            return character;
        });
    }

    public static Optional<CharacterDefinition> replaceCharacter(ServerPlayer player, String characterId) {
        return CharacterBranchPackManager.findCharacter(characterId).map(character -> {
            getSelectedCharacter(player).ifPresent(oldCharacter ->
                    CharacterEffectHooks.onCharacterDeselected(player, oldCharacter));
            player.getPersistentData().remove(NORMAL_PLAYER_TAG);
            player.getPersistentData().putString(SELECTED_CHARACTER_TAG, character.id());
            CharacterSkinSync.syncToTracking(player);
            return character;
        });
    }

    public static boolean forceReselectionIfSelected(ServerPlayer player, String characterId, Component reason) {
        Optional<CharacterDefinition> selected = getSelectedCharacter(player);
        if (selected.isEmpty() || !selected.get().id().equals(characterId)) {
            return false;
        }

        CharacterEffectHooks.onCharacterDeselected(player, selected.get());
        player.getPersistentData().remove(SELECTED_CHARACTER_TAG);
        player.getPersistentData().remove(NORMAL_PLAYER_TAG);
        grantCharacterReselection(player);
        CharacterSkinSync.syncToTracking(player);
        CharacterAvailability.syncToClient(player);
        NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter("", false), player);
        NetworkHandler.sendToPlayer(new S2C_OpenCharacterSelection(true), player);
        if (reason != null) {
            player.displayClientMessage(reason, false);
        }
        return true;
    }

    public static boolean clearSelectedCharacter(ServerPlayer player) {
        return clearSelectedCharacter(player, false);
    }

    public static boolean clearSelectedCharacter(ServerPlayer player, boolean becomeNormalPlayer) {
        Optional<CharacterDefinition> selected = getSelectedCharacter(player);
        if (selected.isEmpty()) {
            return false;
        }
        CharacterEffectHooks.onCharacterDeselected(player, selected.get());
        player.getPersistentData().remove(SELECTED_CHARACTER_TAG);
        player.getPersistentData().remove(CHARACTER_RESELECTION_TAG);
        if (becomeNormalPlayer) {
            player.getPersistentData().putBoolean(NORMAL_PLAYER_TAG, true);
        } else {
            player.getPersistentData().remove(NORMAL_PLAYER_TAG);
        }
        CharacterSkinSync.syncToTracking(player);
        CharacterAvailability.syncToClient(player);
        NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter("", becomeNormalPlayer), player);
        return true;
    }

    public static void copySelectedCharacter(Player original, Player target) {
        Optional<String> selected = getSelectedCharacterId(original);
        selected.ifPresentOrElse(
                id -> {
                    target.getPersistentData().putString(SELECTED_CHARACTER_TAG, id);
                    target.getPersistentData().remove(NORMAL_PLAYER_TAG);
                },
                () -> {
                    target.getPersistentData().remove(SELECTED_CHARACTER_TAG);
                    if (isNormalPlayer(original)) {
                        target.getPersistentData().putBoolean(NORMAL_PLAYER_TAG, true);
                    } else {
                        target.getPersistentData().remove(NORMAL_PLAYER_TAG);
                    }
                }
        );
        if (hasCharacterReselection(original)) {
            target.getPersistentData().putBoolean(CHARACTER_RESELECTION_TAG, true);
        } else {
            target.getPersistentData().remove(CHARACTER_RESELECTION_TAG);
        }
    }
}
