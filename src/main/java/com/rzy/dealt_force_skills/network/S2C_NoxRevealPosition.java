package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientNoxHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_NoxRevealPosition {
    private final int entityId;
    private final Vec3 position;
    private final int ticks;

    public S2C_NoxRevealPosition(int entityId, Vec3 position, int ticks) {
        this.entityId = entityId;
        this.position = position;
        this.ticks = ticks;
    }

    public static void encode(S2C_NoxRevealPosition msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeDouble(msg.position.x);
        buf.writeDouble(msg.position.y);
        buf.writeDouble(msg.position.z);
        buf.writeVarInt(msg.ticks);
    }

    public static S2C_NoxRevealPosition decode(FriendlyByteBuf buf) {
        return new S2C_NoxRevealPosition(
                buf.readVarInt(),
                new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_NoxRevealPosition msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientNoxHudState.revealEntity(msg.entityId, msg.position, msg.ticks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
