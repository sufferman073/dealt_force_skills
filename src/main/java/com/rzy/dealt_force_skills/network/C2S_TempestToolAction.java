package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.character.tempest.TempestSkills;
import com.rzy.dealt_force_skills.character.tempest.TempestStateManager;
import com.rzy.dealt_force_skills.character.tempest.TempestToolAction;
import com.rzy.dealt_force_skills.character.vlinder.VlinderStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_TempestToolAction {
    private final TempestToolAction action;
    private final boolean highThrow;

    public C2S_TempestToolAction(TempestToolAction action) {
        this(action, false);
    }

    public C2S_TempestToolAction(TempestToolAction action, boolean highThrow) {
        this.action = action;
        this.highThrow = highThrow;
    }

    public static void encode(C2S_TempestToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeBoolean(msg.highThrow);
    }

    public static C2S_TempestToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        TempestToolAction[] values = TempestToolAction.values();
        TempestToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : TempestToolAction.STOW_TOOL;
        return new C2S_TempestToolAction(action, buf.readBoolean());
    }

    public static void handle(C2S_TempestToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null
                    || UluruLoiteringMissileEntity.isPlayerControlling(player)
                    || RaptorFalconDroneEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || player.hasEffect(ModEffects.TEMPEST_DISARMED.get())
                    || StingerStateManager.isDowned(player)
                    || VlinderStateManager.isDowned(player)
                    || TempestStateManager.isActionLocked(player)) {
                return;
            }
            TempestSkills.handleToolAction(player, msg.action, msg.highThrow);
            TempestStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
