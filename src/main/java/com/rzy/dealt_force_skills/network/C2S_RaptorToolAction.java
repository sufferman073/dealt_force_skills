package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.raptor.RaptorSkills;
import com.rzy.dealt_force_skills.character.raptor.RaptorStateManager;
import com.rzy.dealt_force_skills.character.raptor.RaptorToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_RaptorToolAction {
    private final RaptorToolAction action;
    private final boolean alternate;

    public C2S_RaptorToolAction(RaptorToolAction action) {
        this(action, false);
    }

    public C2S_RaptorToolAction(RaptorToolAction action, boolean alternate) {
        this.action = action;
        this.alternate = alternate;
    }

    public static void encode(C2S_RaptorToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeBoolean(msg.alternate);
    }

    public static C2S_RaptorToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        RaptorToolAction[] values = RaptorToolAction.values();
        RaptorToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : RaptorToolAction.STOW_TOOL;
        return new C2S_RaptorToolAction(action, buf.readBoolean());
    }

    public static void handle(C2S_RaptorToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null
                    || UluruLoiteringMissileEntity.isPlayerControlling(player)
                    || RaptorFalconDroneEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || StingerStateManager.isDowned(player)) {
                return;
            }
            RaptorSkills.handleToolAction(player, msg.action, msg.alternate);
            RaptorStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
