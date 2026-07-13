package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.chamber.ChamberSkills;
import com.rzy.dealt_force_skills.character.chamber.ChamberStateManager;
import com.rzy.dealt_force_skills.character.chamber.ChamberToolAction;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_ChamberToolAction {
    private final ChamberToolAction action;
    private final int targetEntityId;

    public C2S_ChamberToolAction(ChamberToolAction action) {
        this(action, -1);
    }

    public C2S_ChamberToolAction(ChamberToolAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_ChamberToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_ChamberToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        ChamberToolAction[] values = ChamberToolAction.values();
        ChamberToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : ChamberToolAction.STOW_TOOL;
        return new C2S_ChamberToolAction(action, buf.readVarInt());
    }

    public static void handle(C2S_ChamberToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator()
                    || player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || player.hasEffect(ModEffects.N_TWO_FROZEN.get())
                    || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            ChamberSkills.handleToolAction(player, msg.action, msg.targetEntityId);
            ChamberStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
