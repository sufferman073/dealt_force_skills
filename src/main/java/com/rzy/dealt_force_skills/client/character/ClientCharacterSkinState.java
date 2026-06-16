package com.rzy.dealt_force_skills.client.character;

import com.rzy.dealt_force_skills.character.CharacterSkinTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class ClientCharacterSkinState {
    private static final Map<Integer, String> CHARACTER_BY_ENTITY_ID = new HashMap<>();

    private ClientCharacterSkinState() {
    }

    public static void clear() {
        CHARACTER_BY_ENTITY_ID.clear();
    }

    public static void sync(int entityId, String characterId) {
        if (entityId < 0 || characterId == null || characterId.isBlank()) {
            CHARACTER_BY_ENTITY_ID.remove(entityId);
            return;
        }
        CHARACTER_BY_ENTITY_ID.put(entityId, characterId);
    }

    public static void syncLocalSelectedCharacter(String characterId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            sync(minecraft.player.getId(), characterId);
        }
    }

    public static Optional<ResourceLocation> skinFor(Player player) {
        if (player == null) {
            return Optional.empty();
        }
        String characterId = CHARACTER_BY_ENTITY_ID.get(player.getId());
        if ((characterId == null || characterId.isBlank()) && player == Minecraft.getInstance().player) {
            characterId = ClientCharacterSelectionState.selectedCharacterId();
        }
        return CharacterSkinTextures.skinFor(characterId);
    }

    public static Optional<ResourceLocation> skinFor(AbstractClientPlayer player) {
        return skinFor((Player) player);
    }
}
