package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationSkills;
import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationStateManager;
import com.rzy.dealt_force_skills.character.department.DepartmentToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_DepartmentToolAction {
    private final DepartmentToolAction action;
    private final int targetEntityId;

    public C2S_DepartmentToolAction(DepartmentToolAction action) {
        this(action, -1);
    }

    public C2S_DepartmentToolAction(DepartmentToolAction action, int targetEntityId) {
        this.action = action;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(C2S_DepartmentToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeVarInt(msg.targetEntityId);
    }

    public static C2S_DepartmentToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        DepartmentToolAction[] values = DepartmentToolAction.values();
        DepartmentToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : DepartmentToolAction.STOW_TOOL;
        return new C2S_DepartmentToolAction(action, buf.readVarInt());
    }

    public static void handle(C2S_DepartmentToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (msg.action != DepartmentToolAction.CALIBRATION_RELEASE
                    && (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || StingerStateManager.isDowned(player))) {
                return;
            }
            DepartmentOfTransportationSkills.handleToolAction(player, msg.action, msg.targetEntityId);
            if (DepartmentOfTransportationStateManager.isDepartment(player)) {
                DepartmentOfTransportationStateManager.syncToClient(player);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
