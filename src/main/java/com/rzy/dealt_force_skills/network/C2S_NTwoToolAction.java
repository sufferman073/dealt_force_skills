package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.ntwo.NTwoSkills;
import com.rzy.dealt_force_skills.character.ntwo.NTwoStateManager;
import com.rzy.dealt_force_skills.character.ntwo.NTwoToolAction;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_NTwoToolAction {
    private final NTwoToolAction action;

    public C2S_NTwoToolAction(NTwoToolAction action) {
        this.action = action;
    }

    public static void encode(C2S_NTwoToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
    }

    public static C2S_NTwoToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        NTwoToolAction[] values = NTwoToolAction.values();
        return new C2S_NTwoToolAction(ordinal >= 0 && ordinal < values.length ? values[ordinal] : NTwoToolAction.STOW_TOOL);
    }

    public static void handle(C2S_NTwoToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator()
                    || player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || player.hasEffect(ModEffects.N_TWO_FROZEN.get())
                    || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            NTwoSkills.handleToolAction(player, msg.action);
            NTwoStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
