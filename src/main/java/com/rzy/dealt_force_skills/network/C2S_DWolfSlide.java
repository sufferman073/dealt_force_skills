package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.dwolf.DWolfSkills;
import com.rzy.dealt_force_skills.character.dwolf.DWolfStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_DWolfSlide {
    public C2S_DWolfSlide() {
    }

    public static void encode(C2S_DWolfSlide msg, FriendlyByteBuf buf) {
    }

    public static C2S_DWolfSlide decode(FriendlyByteBuf buf) {
        return new C2S_DWolfSlide();
    }

    public static void handle(C2S_DWolfSlide msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator() || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player) || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            boolean accepted = DWolfSkills.tryTacticalSlide(player);
            if (accepted) {
                NetworkHandler.sendToPlayer(new S2C_DWolfSlideAccepted(), player);
            }
            DWolfStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
