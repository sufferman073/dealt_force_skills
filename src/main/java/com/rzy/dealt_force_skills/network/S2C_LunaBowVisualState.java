package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientLunaBowVisualState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_LunaBowVisualState(
        int entityId,
        int phase,
        int toolOrdinal,
        int remainingTicks
) {
    public static void encode(S2C_LunaBowVisualState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.phase);
        buf.writeVarInt(msg.toolOrdinal);
        buf.writeVarInt(msg.remainingTicks);
    }

    public static S2C_LunaBowVisualState decode(FriendlyByteBuf buf) {
        return new S2C_LunaBowVisualState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_LunaBowVisualState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientLunaBowVisualState.sync(
                        msg.entityId,
                        msg.phase,
                        msg.toolOrdinal,
                        msg.remainingTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
