package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.gizmo.GizmoStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_GizmoWebEscape {
    private final boolean holdingSpace;

    public C2S_GizmoWebEscape(boolean holdingSpace) {
        this.holdingSpace = holdingSpace;
    }

    public static void encode(C2S_GizmoWebEscape msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.holdingSpace);
    }

    public static C2S_GizmoWebEscape decode(FriendlyByteBuf buf) {
        return new C2S_GizmoWebEscape(buf.readBoolean());
    }

    public static void handle(C2S_GizmoWebEscape msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && player.hasEffect(ModEffects.WEBBED.get()) && !StingerStateManager.isDowned(player)) {
                GizmoStateManager.setWebEscapeHolding(player, msg.holdingSpace);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
