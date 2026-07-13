package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.vyron.VyronSkills;
import com.rzy.dealt_force_skills.character.vyron.VyronStateManager;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.team.RoundStartFreezeManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class C2S_VyronDash {
    private final double dirX;
    private final double dirZ;

    public C2S_VyronDash(double dirX, double dirZ) {
        this.dirX = dirX;
        this.dirZ = dirZ;
    }

    public static void encode(C2S_VyronDash msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.dirX);
        buf.writeDouble(msg.dirZ);
    }

    public static C2S_VyronDash decode(FriendlyByteBuf buf) {
        return new C2S_VyronDash(buf.readDouble(), buf.readDouble());
    }

    public static void handle(C2S_VyronDash msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null || player.isSpectator() || UluruLoiteringMissileEntity.isPlayerControlling(player)) {
                return;
            }
            if (player.hasEffect(ModEffects.STUN.get()) || player.hasEffect(ModEffects.WEBBED.get()) || StingerStateManager.isDowned(player) || RoundStartFreezeManager.isFrozen(player)) {
                return;
            }
            VyronSkills.tryDash(player, new Vec3(msg.dirX, 0.0D, msg.dirZ));
            VyronStateManager.syncToClient(player);
        });
        ctx.get().setPacketHandled(true);
    }
}
