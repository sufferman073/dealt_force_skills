package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.ClientEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SuppressLocalHurtAnimation {
    private final int ticks;

    public S2C_SuppressLocalHurtAnimation(int ticks) {
        this.ticks = ticks;
    }

    public static void encode(S2C_SuppressLocalHurtAnimation msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.ticks);
    }

    public static S2C_SuppressLocalHurtAnimation decode(FriendlyByteBuf buf) {
        return new S2C_SuppressLocalHurtAnimation(buf.readVarInt());
    }

    public static void handle(S2C_SuppressLocalHurtAnimation msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientEvents.suppressLocalHurtAnimation(msg.ticks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
