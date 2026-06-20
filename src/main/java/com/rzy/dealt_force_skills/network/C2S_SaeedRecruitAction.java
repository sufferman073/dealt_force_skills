package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.shop.SaeedRecruitAction;
import com.rzy.dealt_force_skills.shop.SaeedRecruitManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_SaeedRecruitAction(SaeedRecruitAction action, String guardTypeId) {
    public static void encode(C2S_SaeedRecruitAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeUtf(msg.guardTypeId == null ? "" : msg.guardTypeId, 64);
    }

    public static C2S_SaeedRecruitAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        SaeedRecruitAction[] values = SaeedRecruitAction.values();
        SaeedRecruitAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : SaeedRecruitAction.RECRUIT;
        return new C2S_SaeedRecruitAction(action, buf.readUtf(64));
    }

    public static void handle(C2S_SaeedRecruitAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                SaeedRecruitManager.handleAction(player, msg.action, msg.guardTypeId);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
