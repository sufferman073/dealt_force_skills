package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.gizmo.GizmoSkills;
import com.rzy.dealt_force_skills.character.gizmo.GizmoStateManager;
import com.rzy.dealt_force_skills.character.gizmo.GizmoToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_GizmoToolAction {
    private final GizmoToolAction action;
    private final int targetEntityId;

    public C2S_GizmoToolAction(GizmoToolAction action) {
        this(action, -1);
    }

    public C2S_GizmoToolAction(GizmoToolAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_GizmoToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_GizmoToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        GizmoToolAction[] values = GizmoToolAction.values();
        GizmoToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : GizmoToolAction.STOW_TOOL;
        return new C2S_GizmoToolAction(action, buf.readVarInt());
    }

    public static void handle(C2S_GizmoToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player)) {
                return;
            }
            GizmoSkills.handleToolAction(player, msg.action, msg.targetEntityId);
            GizmoStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
