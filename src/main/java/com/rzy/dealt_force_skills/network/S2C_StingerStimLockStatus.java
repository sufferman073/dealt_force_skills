package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientStingerHudState;
import com.rzy.dealt_force_skills.client.character.ClientStingerStimLockState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_StingerStimLockStatus {
    private final boolean targetWarning;
    private final int modeOrdinal;
    private final int targetCount;
    private final int durationTicks;

    public S2C_StingerStimLockStatus(boolean targetWarning, int modeOrdinal, int targetCount, int durationTicks) {
        this.targetWarning = targetWarning;
        this.modeOrdinal = modeOrdinal;
        this.targetCount = targetCount;
        this.durationTicks = durationTicks;
    }

    public static void encode(S2C_StingerStimLockStatus msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.targetWarning);
        buf.writeVarInt(msg.modeOrdinal);
        buf.writeVarInt(msg.targetCount);
        buf.writeVarInt(msg.durationTicks);
    }

    public static S2C_StingerStimLockStatus decode(FriendlyByteBuf buf) {
        return new S2C_StingerStimLockStatus(
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_StingerStimLockStatus msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    if (msg.targetWarning) {
                        ClientStingerStimLockState.warn(msg.modeOrdinal, msg.durationTicks);
                    } else {
                        ClientStingerHudState.syncStimLockTargets(msg.targetCount, msg.durationTicks);
                    }
                }
        ));
        ctx.get().setPacketHandled(true);
    }
}
