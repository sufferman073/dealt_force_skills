package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.manba.ManbaSkills;
import com.rzy.dealt_force_skills.character.manba.ManbaStateManager;
import com.rzy.dealt_force_skills.character.manba.ManbaToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_ManbaToolAction {
    private final ManbaToolAction action;
    private final int targetEntityId;

    public C2S_ManbaToolAction(ManbaToolAction action) {
        this(action, -1);
    }

    public C2S_ManbaToolAction(ManbaToolAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_ManbaToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_ManbaToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        ManbaToolAction[] values = ManbaToolAction.values();
        ManbaToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : ManbaToolAction.STOP_FLASHLIGHT;
        return new C2S_ManbaToolAction(action, buf.readVarInt());
    }

    public static void handle(C2S_ManbaToolAction msg, Supplier<NetworkEvent.Context> ctx) {
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
            ManbaSkills.handleToolAction(player, msg.action, msg.targetEntityId);
            ManbaStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
