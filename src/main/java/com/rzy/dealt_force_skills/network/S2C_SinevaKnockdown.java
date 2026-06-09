package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.visual.ClientSinevaKnockdownState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SinevaKnockdown {
    private final int entityId;
    private final int ticks;
    private final float yaw;

    public S2C_SinevaKnockdown(int entityId, int ticks, float yaw) {
        this.entityId = entityId;
        this.ticks = ticks;
        this.yaw = yaw;
    }

    public static void encode(S2C_SinevaKnockdown msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.ticks);
        buf.writeFloat(msg.yaw);
    }

    public static S2C_SinevaKnockdown decode(FriendlyByteBuf buf) {
        return new S2C_SinevaKnockdown(buf.readVarInt(), buf.readVarInt(), buf.readFloat());
    }

    public static void handle(S2C_SinevaKnockdown msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSinevaKnockdownState.sync(msg.entityId, msg.ticks, msg.yaw)
        ));
        ctx.get().setPacketHandled(true);
    }
}
