package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.dwolf.DWolfSkills;
import com.rzy.dealt_force_skills.character.dwolf.DWolfStateManager;
import com.rzy.dealt_force_skills.character.dwolf.DWolfToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_DWolfToolAction {
    private final DWolfToolAction action;

    public C2S_DWolfToolAction(DWolfToolAction action) {
        this.action = action;
    }

    public static void encode(C2S_DWolfToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2S_DWolfToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        DWolfToolAction[] values = DWolfToolAction.values();
        DWolfToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : DWolfToolAction.STOW_TOOL;
        return new C2S_DWolfToolAction(action);
    }

    public static void handle(C2S_DWolfToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator() || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player) || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            DWolfSkills.handleToolAction(player, msg.action);
            DWolfStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
