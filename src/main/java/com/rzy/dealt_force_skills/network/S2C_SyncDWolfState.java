package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientDWolfHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncDWolfState {
    private final int handCannonCharges;
    private final int handCannonMaxCharges;
    private final int handCannonRechargeTicks;
    private final int smokeCharges;
    private final int smokeMaxCharges;
    private final int smokeRechargeTicks;
    private final int overloadCooldownTicks;
    private final int overloadStartupTicks;
    private final int overloadActiveTicks;
    private final int equippedToolOrdinal;
    private final int cannonBurstShots;
    private final int slideTicks;

    public S2C_SyncDWolfState(
            int handCannonCharges,
            int handCannonMaxCharges,
            int handCannonRechargeTicks,
            int smokeCharges,
            int smokeMaxCharges,
            int smokeRechargeTicks,
            int overloadCooldownTicks,
            int overloadStartupTicks,
            int overloadActiveTicks,
            int equippedToolOrdinal,
            int cannonBurstShots,
            int slideTicks
    ) {
        this.handCannonCharges = handCannonCharges;
        this.handCannonMaxCharges = handCannonMaxCharges;
        this.handCannonRechargeTicks = handCannonRechargeTicks;
        this.smokeCharges = smokeCharges;
        this.smokeMaxCharges = smokeMaxCharges;
        this.smokeRechargeTicks = smokeRechargeTicks;
        this.overloadCooldownTicks = overloadCooldownTicks;
        this.overloadStartupTicks = overloadStartupTicks;
        this.overloadActiveTicks = overloadActiveTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.cannonBurstShots = cannonBurstShots;
        this.slideTicks = slideTicks;
    }

    public static void encode(S2C_SyncDWolfState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.handCannonCharges);
        buf.writeVarInt(msg.handCannonMaxCharges);
        buf.writeVarInt(msg.handCannonRechargeTicks);
        buf.writeVarInt(msg.smokeCharges);
        buf.writeVarInt(msg.smokeMaxCharges);
        buf.writeVarInt(msg.smokeRechargeTicks);
        buf.writeVarInt(msg.overloadCooldownTicks);
        buf.writeVarInt(msg.overloadStartupTicks);
        buf.writeVarInt(msg.overloadActiveTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeVarInt(msg.cannonBurstShots);
        buf.writeVarInt(msg.slideTicks);
    }

    public static S2C_SyncDWolfState decode(FriendlyByteBuf buf) {
        return new S2C_SyncDWolfState(
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
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncDWolfState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientDWolfHudState.sync(
                        msg.handCannonCharges,
                        msg.handCannonMaxCharges,
                        msg.handCannonRechargeTicks,
                        msg.smokeCharges,
                        msg.smokeMaxCharges,
                        msg.smokeRechargeTicks,
                        msg.overloadCooldownTicks,
                        msg.overloadStartupTicks,
                        msg.overloadActiveTicks,
                        msg.equippedToolOrdinal,
                        msg.cannonBurstShots,
                        msg.slideTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
