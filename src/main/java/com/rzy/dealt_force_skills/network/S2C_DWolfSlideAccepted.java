package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.DWolfInputHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_DWolfSlideAccepted {
    public S2C_DWolfSlideAccepted() {
    }

    public static void encode(S2C_DWolfSlideAccepted msg, FriendlyByteBuf buf) {
    }

    public static S2C_DWolfSlideAccepted decode(FriendlyByteBuf buf) {
        return new S2C_DWolfSlideAccepted();
    }

    public static void handle(S2C_DWolfSlideAccepted msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> DWolfInputHandler::acceptSlide
        ));
        ctx.get().setPacketHandled(true);
    }
}
