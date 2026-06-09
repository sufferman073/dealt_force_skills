package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientLexNinjiaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2C_SyncLexNinjiaState(
        float leicra,
        float maxLeicra,
        int handStacks,
        int bladeStacks,
        int harmonyStacks,
        String preparedArtId,
        boolean preparedAffordable,
        String comboText,
        int mindUsed,
        int mindCapacity,
        int deathFlameTicks,
        float deathFlameOverflow,
        int hamPowerTicks,
        int hamBerserkTicks
) {
    public static void encode(S2C_SyncLexNinjiaState msg, FriendlyByteBuf buf) {
        buf.writeFloat(msg.leicra);
        buf.writeFloat(msg.maxLeicra);
        buf.writeVarInt(msg.handStacks);
        buf.writeVarInt(msg.bladeStacks);
        buf.writeVarInt(msg.harmonyStacks);
        buf.writeUtf(msg.preparedArtId, 64);
        buf.writeBoolean(msg.preparedAffordable);
        buf.writeUtf(msg.comboText, 256);
        buf.writeVarInt(msg.mindUsed);
        buf.writeVarInt(msg.mindCapacity);
        buf.writeVarInt(msg.deathFlameTicks);
        buf.writeFloat(msg.deathFlameOverflow);
        buf.writeVarInt(msg.hamPowerTicks);
        buf.writeVarInt(msg.hamBerserkTicks);
    }

    public static S2C_SyncLexNinjiaState decode(FriendlyByteBuf buf) {
        return new S2C_SyncLexNinjiaState(
                buf.readFloat(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readUtf(64),
                buf.readBoolean(),
                buf.readUtf(256),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readFloat(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncLexNinjiaState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientLexNinjiaHudState.sync(
                        msg.leicra,
                        msg.maxLeicra,
                        msg.handStacks,
                        msg.bladeStacks,
                        msg.harmonyStacks,
                        msg.preparedArtId,
                        msg.preparedAffordable,
                        msg.comboText,
                        msg.mindUsed,
                        msg.mindCapacity,
                        msg.deathFlameTicks,
                        msg.deathFlameOverflow,
                        msg.hamPowerTicks,
                        msg.hamBerserkTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
