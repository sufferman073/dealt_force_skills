package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.corps.CorpsSkills;
import com.rzy.dealt_force_skills.character.corps.CorpsStateManager;
import com.rzy.dealt_force_skills.character.corps.CorpsToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_CorpsToolAction {
    private final CorpsToolAction action;
    private final int targetEntityId;

    public C2S_CorpsToolAction(CorpsToolAction action) {
        this(action, -1);
    }

    public C2S_CorpsToolAction(CorpsToolAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_CorpsToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_CorpsToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        CorpsToolAction[] values = CorpsToolAction.values();
        CorpsToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : CorpsToolAction.CANCEL_DUEL;
        return new C2S_CorpsToolAction(action, buf.readVarInt());
    }

    public static void handle(C2S_CorpsToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator() || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || player.hasEffect(ModEffects.N_TWO_FROZEN.get())
                    || StingerStateManager.isDowned(player)
                    || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            CorpsSkills.handleToolAction(player, msg.action, msg.targetEntityId);
            CorpsStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
