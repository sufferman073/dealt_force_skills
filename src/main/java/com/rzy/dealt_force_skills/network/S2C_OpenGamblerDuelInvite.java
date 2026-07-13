package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.GamblerDuelInviteScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_OpenGamblerDuelInvite {
    public S2C_OpenGamblerDuelInvite() {
    }

    public static void encode(S2C_OpenGamblerDuelInvite msg, FriendlyByteBuf buf) {
    }

    public static S2C_OpenGamblerDuelInvite decode(FriendlyByteBuf buf) {
        return new S2C_OpenGamblerDuelInvite();
    }

    public static void handle(S2C_OpenGamblerDuelInvite msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> GamblerDuelInviteScreen::open));
        ctx.get().setPacketHandled(true);
    }
}
