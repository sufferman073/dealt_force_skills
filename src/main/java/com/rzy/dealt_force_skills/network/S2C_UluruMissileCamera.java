package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.UluruMissileController;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_UluruMissileCamera {
    private final int entityId;
    private final boolean active;

    public S2C_UluruMissileCamera(int entityId, boolean active) {
        this.entityId = entityId;
        this.active = active;
    }

    public static void encode(S2C_UluruMissileCamera msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.active);
    }

    public static S2C_UluruMissileCamera decode(FriendlyByteBuf buf) {
        return new S2C_UluruMissileCamera(buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(S2C_UluruMissileCamera msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    if (msg.active) {
                        UluruMissileController.start(msg.entityId);
                    } else {
                        UluruMissileController.stop(false);
                    }
                }
        ));
        ctx.get().setPacketHandled(true);
    }
}
