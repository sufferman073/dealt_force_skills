package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientDWolfHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_DWolfStaminaRestore {
    private final int percent;

    public S2C_DWolfStaminaRestore(int percent) {
        this.percent = percent;
    }

    public static void encode(S2C_DWolfStaminaRestore msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.percent);
    }

    public static S2C_DWolfStaminaRestore decode(FriendlyByteBuf buf) {
        return new S2C_DWolfStaminaRestore(buf.readVarInt());
    }

    public static void handle(S2C_DWolfStaminaRestore msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientDWolfHudState.restoreStaminaPercent(msg.percent)
        ));
        ctx.get().setPacketHandled(true);
    }
}
