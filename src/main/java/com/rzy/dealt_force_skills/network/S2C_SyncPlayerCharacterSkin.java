package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientCharacterSkinState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncPlayerCharacterSkin {
    private final int entityId;
    private final String characterId;

    public S2C_SyncPlayerCharacterSkin(int entityId, String characterId) {
        this.entityId = entityId;
        this.characterId = characterId == null ? "" : characterId;
    }

    public static void encode(S2C_SyncPlayerCharacterSkin msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeUtf(msg.characterId);
    }

    public static S2C_SyncPlayerCharacterSkin decode(FriendlyByteBuf buf) {
        return new S2C_SyncPlayerCharacterSkin(buf.readVarInt(), buf.readUtf(256));
    }

    public static void handle(S2C_SyncPlayerCharacterSkin msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientCharacterSkinState.sync(msg.entityId, msg.characterId)
        ));
        ctx.get().setPacketHandled(true);
    }
}
