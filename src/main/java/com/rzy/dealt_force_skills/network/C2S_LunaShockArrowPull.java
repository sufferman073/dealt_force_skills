package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.LunaShockArrowEntity;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_LunaShockArrowPull {
    private final boolean holding;

    public C2S_LunaShockArrowPull(boolean holding) {
        this.holding = holding;
    }

    public static void encode(C2S_LunaShockArrowPull msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.holding);
    }

    public static C2S_LunaShockArrowPull decode(FriendlyByteBuf buf) {
        return new C2S_LunaShockArrowPull(buf.readBoolean());
    }

    public static void handle(C2S_LunaShockArrowPull msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null && !player.isSpectator() && !StingerStateManager.isDowned(player)
                    && !RoundStartFreezeManager.isFrozen(player)) {
                LunaShockArrowEntity.reportPullHold(player, msg.holding);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
