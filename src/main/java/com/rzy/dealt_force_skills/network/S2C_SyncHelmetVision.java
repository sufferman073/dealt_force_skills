package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.HelmetVisionClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncHelmetVision {
    private final int mode;

    public S2C_SyncHelmetVision(int mode) {
        this.mode = mode;
    }

    public static void encode(S2C_SyncHelmetVision msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.mode);
    }

    public static S2C_SyncHelmetVision decode(FriendlyByteBuf buf) {
        return new S2C_SyncHelmetVision(buf.readVarInt());
    }

    public static void handle(S2C_SyncHelmetVision msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HelmetVisionClient.syncLocalHelmetMode(msg.mode)
        ));
        ctx.get().setPacketHandled(true);
    }
}
