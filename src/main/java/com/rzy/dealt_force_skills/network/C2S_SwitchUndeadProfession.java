package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.undead.UndeadProfession;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record C2S_SwitchUndeadProfession(int professionOrdinal) {
    public static void encode(C2S_SwitchUndeadProfession msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.professionOrdinal);
    }

    public static C2S_SwitchUndeadProfession decode(FriendlyByteBuf buf) {
        return new C2S_SwitchUndeadProfession(buf.readVarInt());
    }

    public static void handle(C2S_SwitchUndeadProfession msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && !player.isSpectator() && UndeadStateManager.isUndead(player)) {
                UndeadStateManager.switchProfession(player,
                        UndeadProfession.byOrdinal(msg.professionOrdinal));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
