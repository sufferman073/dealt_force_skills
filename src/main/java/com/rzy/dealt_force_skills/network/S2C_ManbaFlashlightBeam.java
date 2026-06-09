package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.visual.ManbaFlashlightBeamRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_ManbaFlashlightBeam {
    private final int entityId;
    private final boolean active;
    private final float range;
    private final float halfAngleDegrees;
    private final int ticks;

    public S2C_ManbaFlashlightBeam(int entityId, boolean active, float range, float halfAngleDegrees, int ticks) {
        this.entityId = entityId;
        this.active = active;
        this.range = range;
        this.halfAngleDegrees = halfAngleDegrees;
        this.ticks = ticks;
    }

    public static void encode(S2C_ManbaFlashlightBeam msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.active);
        buf.writeFloat(msg.range);
        buf.writeFloat(msg.halfAngleDegrees);
        buf.writeVarInt(msg.ticks);
    }

    public static S2C_ManbaFlashlightBeam decode(FriendlyByteBuf buf) {
        return new S2C_ManbaFlashlightBeam(
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_ManbaFlashlightBeam msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ManbaFlashlightBeamRenderer.syncBeam(
                        msg.entityId,
                        msg.active,
                        msg.range,
                        msg.halfAngleDegrees,
                        msg.ticks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
