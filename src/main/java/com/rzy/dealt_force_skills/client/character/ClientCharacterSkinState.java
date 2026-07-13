package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.CharacterSkinTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class ClientCharacterSkinState {
    private static final Map<Integer, String> CHARACTER_BY_ENTITY_ID = new HashMap<>();
    private static final Set<Integer> DISABLED_ENTITY_IDS = new HashSet<>();

    private ClientCharacterSkinState() {
    }

    public static void clear() {
        CHARACTER_BY_ENTITY_ID.clear();
        DISABLED_ENTITY_IDS.clear();
    }

    public static void sync(int entityId, String characterId) {
        sync(entityId, characterId, true);
    }

    public static void sync(int entityId, String characterId, boolean skinsEnabled) {
        if (entityId < 0) {
            return;
        }
        if (!skinsEnabled) {
            if (characterId == null || characterId.isBlank()) {
                CHARACTER_BY_ENTITY_ID.remove(entityId);
            } else {
                CHARACTER_BY_ENTITY_ID.put(entityId, characterId);
            }
            DISABLED_ENTITY_IDS.add(entityId);
            return;
        }
        DISABLED_ENTITY_IDS.remove(entityId);
        if (entityId < 0 || characterId == null || characterId.isBlank()) {
            CHARACTER_BY_ENTITY_ID.remove(entityId);
            return;
        }
        CHARACTER_BY_ENTITY_ID.put(entityId, characterId);
    }

    public static void syncLocalSelectedCharacter(String characterId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            if (DISABLED_ENTITY_IDS.contains(minecraft.player.getId())) {
                CHARACTER_BY_ENTITY_ID.remove(minecraft.player.getId());
                return;
            }
            sync(minecraft.player.getId(), characterId);
        }
    }

    public static Optional<ResourceLocation> skinFor(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        if (DISABLED_ENTITY_IDS.contains(player.getId())) {
            return Optional.empty();
        }
        String characterId = CHARACTER_BY_ENTITY_ID.get(player.getId());
        if ((characterId == null || characterId.isBlank()) && player == Minecraft.getInstance().player) {
            characterId = ClientCharacterSelectionState.selectedCharacterId();
        }
        return CharacterSkinTextures.skinFor(characterId);
    }

    public static Optional<String> characterIdFor(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        String characterId = CHARACTER_BY_ENTITY_ID.get(player.getId());
        return characterId == null || characterId.isBlank() ? Optional.empty() : Optional.of(characterId);
    }

    public static Optional<ResourceLocation> skinFor(AbstractClientPlayer player) {
        return skinFor((Player) player);
    }
}
