package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.character.toxik.ToxikSkills;
import com.rzy.dealt_force_skills.character.toxik.ToxikStateManager;
import com.rzy.dealt_force_skills.character.toxik.ToxikToolAction;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_ToxikToolAction {
    private final ToxikToolAction action;
    private final boolean alternate;

    public C2S_ToxikToolAction(ToxikToolAction action) {
        this(action, false);
    }

    public C2S_ToxikToolAction(ToxikToolAction action, boolean alternate) {
        this.action = action;
        this.alternate = alternate;
    }

    public static void encode(C2S_ToxikToolAction msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.action.ordinal());
        buf.writeBoolean(msg.alternate);
    }

    public static C2S_ToxikToolAction decode(FriendlyByteBuf buf) {
        int ordinal = buf.readVarInt();
        ToxikToolAction[] values = ToxikToolAction.values();
        ToxikToolAction action = ordinal >= 0 && ordinal < values.length ? values[ordinal] : ToxikToolAction.STOW_TOOL;
        return new C2S_ToxikToolAction(action, buf.readBoolean());
    }

    public static void handle(C2S_ToxikToolAction msg, Supplier<NetworkEvent.Context> ctx) {
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
            ToxikSkills.handleToolAction(player, msg.action, msg.alternate);
            ToxikStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
