package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.SaeedMonitorScreen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_OpenSaeedMonitorScreen(CompoundTag data) {
    public S2C_OpenSaeedMonitorScreen {
        data = data == null ? new CompoundTag() : data.copy();
    }

    public static void encode(S2C_OpenSaeedMonitorScreen msg, FriendlyByteBuf buf) {
        buf.writeNbt(msg.data);
    }

    public static S2C_OpenSaeedMonitorScreen decode(FriendlyByteBuf buf) {
        return new S2C_OpenSaeedMonitorScreen(buf.readNbt());
    }

    public static void handle(S2C_OpenSaeedMonitorScreen msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> SaeedMonitorScreen.open(msg.data)
        ));
        ctx.get().setPacketHandled(true);
    }
}
