package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.hackclaw.HackclawSkills;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawStateManager;
import com.rzy.dealt_force_skills.character.hackclaw.HackclawToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_HackclawToolAction {
    private final HackclawToolAction action;
    private final int targetEntityId;

    public C2S_HackclawToolAction(HackclawToolAction action) {
        this(action, -1);
    }

    public C2S_HackclawToolAction(HackclawToolAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_HackclawToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_HackclawToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        HackclawToolAction[] values = HackclawToolAction.values();
        HackclawToolAction action = ordinal >= 0 && ordinal < values.length
                ? values[ordinal]
                : HackclawToolAction.STOW_TOOL;
        return new C2S_HackclawToolAction(action, buf.readVarInt());
    }

    public static void handle(C2S_HackclawToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator() || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || StingerStateManager.isDowned(player)
                    || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            HackclawSkills.handleToolAction(player, msg.action, msg.targetEntityId);
            HackclawStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
