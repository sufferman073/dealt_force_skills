package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaInputAction;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_LexNinjiaInput(LexNinjiaInputAction action) {
    public static void encode(C2S_LexNinjiaInput msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2S_LexNinjiaInput decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        LexNinjiaInputAction[] values = LexNinjiaInputAction.values();
        LexNinjiaInputAction action = ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : LexNinjiaInputAction.SNEAK_RELEASE;
        return new C2S_LexNinjiaInput(action);
    }

    public static void handle(C2S_LexNinjiaInput msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && !player.isSpectator()) {
                LexNinjiaStateManager.handleInput(player, msg.action);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
