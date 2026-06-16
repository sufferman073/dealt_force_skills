package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientHackclawCoreVisualState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_HackclawCoreVisualState(
        int entityId,
        int phase,
        int remainingTicks,
        int round
) {
    public static void encode(S2C_HackclawCoreVisualState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.phase);
        buf.writeVarInt(msg.remainingTicks);
        buf.writeVarInt(msg.round);
    }

    public static S2C_HackclawCoreVisualState decode(FriendlyByteBuf buf) {
        return new S2C_HackclawCoreVisualState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_HackclawCoreVisualState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientHackclawCoreVisualState.sync(
                        msg.entityId,
                        msg.phase,
                        msg.remainingTicks,
                        msg.round
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
