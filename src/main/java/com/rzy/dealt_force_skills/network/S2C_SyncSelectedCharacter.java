package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncSelectedCharacter {
    private final String characterId;

    public S2C_SyncSelectedCharacter(String characterId) {
        this.characterId = characterId == null ? "" : characterId;
    }

    public static void encode(S2C_SyncSelectedCharacter msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.characterId);
    }

    public static S2C_SyncSelectedCharacter decode(FriendlyByteBuf buf) {
        return new S2C_SyncSelectedCharacter(buf.readUtf(256));
    }

    public static void handle(S2C_SyncSelectedCharacter msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientCharacterSelectionState.syncSelectedCharacter(msg.characterId)
        ));
        ctx.get().setPacketHandled(true);
    }
}
