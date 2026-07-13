package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.item.EternalLoveBlessingItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_EternalLoveBlessingCapture {
    private final boolean offhandSource;

    public C2S_EternalLoveBlessingCapture(boolean offhandSource) {
        this.offhandSource = offhandSource;
    }

    public static void encode(C2S_EternalLoveBlessingCapture msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.offhandSource);
    }

    public static C2S_EternalLoveBlessingCapture decode(FriendlyByteBuf buf) {
        return new C2S_EternalLoveBlessingCapture(buf.readBoolean());
    }

    public static void handle(C2S_EternalLoveBlessingCapture msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                EternalLoveBlessingItem.handleCapture(player, msg.offhandSource);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
