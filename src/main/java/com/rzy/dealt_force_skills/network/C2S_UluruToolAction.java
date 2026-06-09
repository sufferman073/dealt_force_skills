package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.uluru.UluruSkills;
import com.rzy.dealt_force_skills.character.uluru.UluruStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_UluruToolAction {
    private final boolean secondary;
    private final boolean guidedLaunch;

    public C2S_UluruToolAction(boolean secondary, boolean guidedLaunch) {
        this.secondary = secondary;
        this.guidedLaunch = guidedLaunch;
    }

    public static void encode(C2S_UluruToolAction msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.secondary);
        buf.writeBoolean(msg.guidedLaunch);
    }

    public static C2S_UluruToolAction decode(FriendlyByteBuf buf) {
        return new C2S_UluruToolAction(buf.readBoolean(), buf.readBoolean());
    }

    public static void handle(C2S_UluruToolAction msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player)) {
                return;
            }
            if (UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            UluruSkills.handleToolAction(player, msg.secondary, msg.guidedLaunch);
            UluruStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
