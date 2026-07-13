package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.morse.MorseSkills;
import com.rzy.dealt_force_skills.character.morse.MorseStateManager;
import com.rzy.dealt_force_skills.character.morse.MorseToolAction;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_MorseToolAction {
    private final MorseToolAction action;
    private final boolean alternate;

    public C2S_MorseToolAction(MorseToolAction action) {
        this(action, false);
    }

    public C2S_MorseToolAction(MorseToolAction action, boolean alternate) {
        this.action = action;
        this.alternate = alternate;
    }

    public static void encode(C2S_MorseToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeBoolean(msg.alternate);
    }

    public static C2S_MorseToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        MorseToolAction[] values = MorseToolAction.values();
        MorseToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : MorseToolAction.STOW_TOOL;
        return new C2S_MorseToolAction(action, buf.readBoolean());
    }

    public static void handle(C2S_MorseToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null
                    || player.isSpectator()
                    || UluruLoiteringMissileEntity.isPlayerControlling(player)
                    || RaptorFalconDroneEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get())
                    || player.hasEffect(ModEffects.WEBBED.get())
                    || StingerStateManager.isDowned(player)
                    || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            MorseStateManager.recordPlayerAction(player);
            MorseSkills.handleToolAction(player, msg.action, msg.alternate);
            MorseStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
