package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.visual.NTwoFrozenVisuals;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_NTwoFrozenVisualState {
    private final int entityId;
    private final int remainingTicks;

    public S2C_NTwoFrozenVisualState(int entityId, int remainingTicks) {
        this.entityId = entityId;
        this.remainingTicks = Math.max(0, remainingTicks);
    }

    public static void encode(S2C_NTwoFrozenVisualState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.remainingTicks);
    }

    public static S2C_NTwoFrozenVisualState decode(FriendlyByteBuf buf) {
        return new S2C_NTwoFrozenVisualState(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(S2C_NTwoFrozenVisualState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> NTwoFrozenVisuals.syncFrozenVisual(msg.entityId, msg.remainingTicks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
