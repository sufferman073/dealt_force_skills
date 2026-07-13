package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.sineva.SinevaSkills;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public final class C2S_SinevaGrappleChargeSound {
    public enum Action {
        START,
        READY,
        RELEASE,
        CANCEL
    }

    private final Action action;

    public C2S_SinevaGrappleChargeSound(Action action) {
        this.action = action;
    }

    public static void encode(C2S_SinevaGrappleChargeSound message, FriendlyByteBuf buffer) {
        buffer.writeEnum(message.action);
    }

    public static C2S_SinevaGrappleChargeSound decode(FriendlyByteBuf buffer) {
        return new C2S_SinevaGrappleChargeSound(buffer.readEnum(Action.class));
    }

    public static void handle(C2S_SinevaGrappleChargeSound message, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null || player.isSpectator()) {
                return;
            }
            switch (message.action) {
                case START -> SinevaSkills.beginGrappleChargeSound(player);
                case READY -> SinevaSkills.markGrappleChargeSoundReady(player);
                case RELEASE -> SinevaSkills.finishGrappleChargeSound(player);
                case CANCEL -> SinevaSkills.cancelGrappleChargeSound(player);
            }
        });
        context.setPacketHandled(true);
    }
}
