package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.sineva.SinevaSkills;
import com.rzy.dealt_force_skills.character.sineva.SinevaStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_SinevaShieldBash {
    public static void encode(C2S_SinevaShieldBash msg, FriendlyByteBuf buf) {
    }

    public static C2S_SinevaShieldBash decode(FriendlyByteBuf buf) {
        return new C2S_SinevaShieldBash();
    }

    public static void handle(C2S_SinevaShieldBash msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player)) return;
            SinevaSkills.tryShieldBash(player);
            SinevaStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
