package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncSelectedCharacter {
    private final String characterId;
    private final boolean normalPlayer;

    public S2C_SyncSelectedCharacter(String characterId) {
        this(characterId, false);
    }

    public S2C_SyncSelectedCharacter(String characterId, boolean normalPlayer) {
        this.characterId = characterId == null ? "" : characterId;
        this.normalPlayer = normalPlayer && this.characterId.isBlank();
    }

    public static void encode(S2C_SyncSelectedCharacter msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.characterId);
        buf.writeBoolean(msg.normalPlayer);
    }

    public static S2C_SyncSelectedCharacter decode(FriendlyByteBuf buf) {
        String characterId = buf.readUtf(256);
        boolean normalPlayer = buf.readBoolean();
        return new S2C_SyncSelectedCharacter(characterId, normalPlayer);
    }

    public static void handle(S2C_SyncSelectedCharacter msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientCharacterSelectionState.syncSelectedCharacter(msg.characterId, msg.normalPlayer)
        ));
        ctx.get().setPacketHandled(true);
    }
}
