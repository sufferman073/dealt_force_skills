package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientUndeadHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_SyncUndeadSouls(long souls) {
    public S2C_SyncUndeadSouls {
        souls = Math.max(0L, souls);
    }

    public static void encode(S2C_SyncUndeadSouls msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.souls);
    }

    public static S2C_SyncUndeadSouls decode(FriendlyByteBuf buf) {
        return new S2C_SyncUndeadSouls(buf.readLong());
    }

    public static void handle(S2C_SyncUndeadSouls msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientUndeadHudState.syncSouls(msg.souls)
        ));
        ctx.get().setPacketHandled(true);
    }
}
