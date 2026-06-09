package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.catdad.CatDadStateManager;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroSkills;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroStateManager;
import com.rzy.dealt_force_skills.character.nikaidou.NikaidouHiroToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.character.tempest.TempestStateManager;
import com.rzy.dealt_force_skills.character.vlinder.VlinderStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_NikaidouHiroToolAction {
    private final NikaidouHiroToolAction action;

    public C2S_NikaidouHiroToolAction(NikaidouHiroToolAction action) {
        this.action = action;
    }

    public static void encode(C2S_NikaidouHiroToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2S_NikaidouHiroToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        NikaidouHiroToolAction[] values = NikaidouHiroToolAction.values();
        return new C2S_NikaidouHiroToolAction(ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : NikaidouHiroToolAction.ATTACK);
    }

    public static void handle(C2S_NikaidouHiroToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null
                    || UluruLoiteringMissileEntity.isPlayerControlling(player)
                    || RaptorFalconDroneEntity.isPlayerControlling(player)
                    || player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || player.hasEffect(ModEffects.TEMPEST_DISARMED.get())
                    || StingerStateManager.isDowned(player)
                    || VlinderStateManager.isDowned(player)
                    || TempestStateManager.isActionLocked(player)
                    || CatDadStateManager.isDowned(player)) {
                return;
            }
            NikaidouHiroSkills.handleToolAction(player, msg.action);
            NikaidouHiroStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
