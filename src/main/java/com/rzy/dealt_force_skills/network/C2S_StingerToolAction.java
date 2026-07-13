package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.stinger.StingerSkills;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerToolAction;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_StingerToolAction {
    private final StingerToolAction action;
    private final boolean alternate;

    public C2S_StingerToolAction(StingerToolAction action) {
        this(action, false);
    }

    public C2S_StingerToolAction(StingerToolAction action, boolean alternate) {
        this.action = action;
        this.alternate = alternate;
    }

    public static void encode(C2S_StingerToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeBoolean(msg.alternate);
    }

    public static C2S_StingerToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        StingerToolAction[] values = StingerToolAction.values();
        StingerToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : StingerToolAction.STOW_TOOL;
        return new C2S_StingerToolAction(action, buf.readBoolean());
    }

    public static void handle(C2S_StingerToolAction msg, Supplier<NetworkEvent.Context> ctx) {
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
            StingerSkills.handleToolAction(player, msg.action, msg.alternate);
            StingerStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
