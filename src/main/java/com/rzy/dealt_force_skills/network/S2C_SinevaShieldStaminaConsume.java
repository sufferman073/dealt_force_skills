package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SinevaShieldStaminaConsume {
    private final int percent;
    private final int floorPercent;

    public S2C_SinevaShieldStaminaConsume(int percent, int floorPercent) {
        this.percent = percent;
        this.floorPercent = floorPercent;
    }

    public static void encode(S2C_SinevaShieldStaminaConsume msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.percent);
        buf.writeVarInt(msg.floorPercent);
    }

    public static S2C_SinevaShieldStaminaConsume decode(FriendlyByteBuf buf) {
        return new S2C_SinevaShieldStaminaConsume(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(S2C_SinevaShieldStaminaConsume msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSinevaHudState.consumeShieldBlockStamina(msg.percent, msg.floorPercent)
        ));
        ctx.get().setPacketHandled(true);
    }
}
