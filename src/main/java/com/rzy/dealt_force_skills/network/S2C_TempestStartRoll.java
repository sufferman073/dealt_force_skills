package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.ClientTempestParCoolBridge;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_TempestStartRoll {
    public static void encode(S2C_TempestStartRoll msg, FriendlyByteBuf buf) {
    }

    public static S2C_TempestStartRoll decode(FriendlyByteBuf buf) {
        return new S2C_TempestStartRoll();
    }

    public static void handle(S2C_TempestStartRoll msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> ClientTempestParCoolBridge::startRoll
        ));
        ctx.get().setPacketHandled(true);
    }
}
