package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientHeldToolVisualState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_SyncHeldToolVisual(int entityId, int visualId) {
    public static void encode(S2C_SyncHeldToolVisual msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.visualId);
    }

    public static S2C_SyncHeldToolVisual decode(FriendlyByteBuf buf) {
        return new S2C_SyncHeldToolVisual(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(S2C_SyncHeldToolVisual msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientHeldToolVisualState.sync(msg.entityId, msg.visualId)
        ));
        ctx.get().setPacketHandled(true);
    }
}
