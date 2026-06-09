package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.nox.NoxSkills;
import com.rzy.dealt_force_skills.character.nox.NoxStateManager;
import com.rzy.dealt_force_skills.character.nox.NoxToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_NoxToolAction {
    private final NoxToolAction action;
    private final int targetEntityId;
    private final boolean alternate;

    public C2S_NoxToolAction(NoxToolAction action) {
        this(action, -1, false);
    }

    public C2S_NoxToolAction(NoxToolAction action, int targetEntityId, boolean alternate) {
        this.action = action;
        this.targetEntityId = targetEntityId;
        this.alternate = alternate;
    }

    public static void encode(C2S_NoxToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
        buf.writeBoolean(msg.alternate);
    }

    public static C2S_NoxToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        NoxToolAction[] values = NoxToolAction.values();
        NoxToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : NoxToolAction.STOW_TOOL;
        return new C2S_NoxToolAction(action, buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(C2S_NoxToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || StingerStateManager.isDowned(player)) {
                return;
            }
            NoxSkills.handleToolAction(player, msg.action, msg.targetEntityId, msg.alternate);
            NoxStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
