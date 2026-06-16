package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientHackclawHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncHackclawState {
    private final int knifeCharges;
    private final int knifeMaxCharges;
    private final int knifeRechargeTicks;
    private final int flashDroneCharges;
    private final int flashDroneMaxCharges;
    private final int flashDroneRechargeTicks;
    private final int coreCooldownTicks;
    private final int coreChannelTicks;
    private final int coreActiveTicks;
    private final int coreRound;
    private final boolean coreScanFound;
    private final int equippedToolOrdinal;

    public S2C_SyncHackclawState(
            int knifeCharges,
            int knifeMaxCharges,
            int knifeRechargeTicks,
            int flashDroneCharges,
            int flashDroneMaxCharges,
            int flashDroneRechargeTicks,
            int coreCooldownTicks,
            int coreChannelTicks,
            int coreActiveTicks,
            int coreRound,
            boolean coreScanFound,
            int equippedToolOrdinal
    ) {
        this.knifeCharges = knifeCharges;
        this.knifeMaxCharges = knifeMaxCharges;
        this.knifeRechargeTicks = knifeRechargeTicks;
        this.flashDroneCharges = flashDroneCharges;
        this.flashDroneMaxCharges = flashDroneMaxCharges;
        this.flashDroneRechargeTicks = flashDroneRechargeTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.coreChannelTicks = coreChannelTicks;
        this.coreActiveTicks = coreActiveTicks;
        this.coreRound = coreRound;
        this.coreScanFound = coreScanFound;
        this.equippedToolOrdinal = equippedToolOrdinal;
    }

    public static void encode(S2C_SyncHackclawState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.knifeCharges);
        buf.writeVarInt(msg.knifeMaxCharges);
        buf.writeVarInt(msg.knifeRechargeTicks);
        buf.writeVarInt(msg.flashDroneCharges);
        buf.writeVarInt(msg.flashDroneMaxCharges);
        buf.writeVarInt(msg.flashDroneRechargeTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.coreChannelTicks);
        buf.writeVarInt(msg.coreActiveTicks);
        buf.writeVarInt(msg.coreRound);
        buf.writeBoolean(msg.coreScanFound);
        buf.writeVarInt(msg.equippedToolOrdinal);
    }

    public static S2C_SyncHackclawState decode(FriendlyByteBuf buf) {
        return new S2C_SyncHackclawState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncHackclawState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientHackclawHudState.sync(
                        msg.knifeCharges,
                        msg.knifeMaxCharges,
                        msg.knifeRechargeTicks,
                        msg.flashDroneCharges,
                        msg.flashDroneMaxCharges,
                        msg.flashDroneRechargeTicks,
                        msg.coreCooldownTicks,
                        msg.coreChannelTicks,
                        msg.coreActiveTicks,
                        msg.coreRound,
                        msg.coreScanFound,
                        msg.equippedToolOrdinal
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
