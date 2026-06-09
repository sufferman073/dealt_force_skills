package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.skill.SkillDispatcher;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_SelectCharacter {
    private final String characterId;

    public C2S_SelectCharacter(String characterId) {
        this.characterId = characterId;
    }

    public static void encode(C2S_SelectCharacter msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.characterId);
    }

    public static C2S_SelectCharacter decode(FriendlyByteBuf buf) {
        return new C2S_SelectCharacter(buf.readUtf(256));
    }

    public static void handle(C2S_SelectCharacter msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            var currentCharacterId = CharacterSelectionManager.getSelectedCharacterId(player);
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player)) {
                NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(currentCharacterId.orElse("")), player);
                player.displayClientMessage(Component.translatable("message.dealt_force_skills.stunned"), true);
                return;
            }
            if (UluruLoiteringMissileEntity.isPlayerControlling(player)
                    || RaptorFalconDroneEntity.isPlayerControlling(player)) {
                NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(currentCharacterId.orElse("")), player);
                return;
            }

            if (currentCharacterId.isPresent()) {
                if (!player.getAbilities().instabuild) {
                    NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(currentCharacterId.get()), player);
                    player.displayClientMessage(Component.translatable("message.dealt_force_skills.selection_locked"), false);
                    return;
                }

                CharacterSelectionManager.replaceCharacter(player, msg.characterId).ifPresentOrElse(
                        character -> {
                            SkillDispatcher.onCharacterSelected(player, character);
                            NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(character.id()), player);
                        },
                        () -> {
                            NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(currentCharacterId.get()), player);
                            player.displayClientMessage(Component.translatable("message.dealt_force_skills.invalid_character"), false);
                        }
                );
                return;
            }

            CharacterSelectionManager.selectCharacter(player, msg.characterId).ifPresentOrElse(
                    character -> {
                        SkillDispatcher.onCharacterSelected(player, character);
                        NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(character.id()), player);
                    },
                    () -> {
                        NetworkHandler.sendToPlayer(new S2C_SyncSelectedCharacter(""), player);
                        player.displayClientMessage(Component.translatable("message.dealt_force_skills.invalid_character"), false);
                    }
            );
        });
        ctx.get().setPacketHandled(true);
    }
}
