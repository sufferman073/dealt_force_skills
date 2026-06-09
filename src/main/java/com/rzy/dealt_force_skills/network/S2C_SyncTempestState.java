package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientTempestHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncTempestState {
    private final int wallCharges;
    private final int wallMaxCharges;
    private final int wallRechargeTicks;
    private final int rollCooldownTicks;
    private final int coreCooldownTicks;
    private final int equippedToolOrdinal;
    private final boolean ropeActive;
    private final double ropeRemaining;
    private final boolean recalling;
    private final boolean pendingLandingRoll;
    private final int downedTicks;
    private final int selfRescueTicks;
    private final int selfRescueRequiredTicks;

    public S2C_SyncTempestState(
            int wallCharges,
            int wallMaxCharges,
            int wallRechargeTicks,
            int rollCooldownTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal,
            boolean ropeActive,
            double ropeRemaining,
            boolean recalling,
            boolean pendingLandingRoll,
            int downedTicks,
            int selfRescueTicks,
            int selfRescueRequiredTicks
    ) {
        this.wallCharges = wallCharges;
        this.wallMaxCharges = wallMaxCharges;
        this.wallRechargeTicks = wallRechargeTicks;
        this.rollCooldownTicks = rollCooldownTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.ropeActive = ropeActive;
        this.ropeRemaining = ropeRemaining;
        this.recalling = recalling;
        this.pendingLandingRoll = pendingLandingRoll;
        this.downedTicks = downedTicks;
        this.selfRescueTicks = selfRescueTicks;
        this.selfRescueRequiredTicks = selfRescueRequiredTicks;
    }

    public static void encode(S2C_SyncTempestState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.wallCharges);
        buf.writeVarInt(msg.wallMaxCharges);
        buf.writeVarInt(msg.wallRechargeTicks);
        buf.writeVarInt(msg.rollCooldownTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeBoolean(msg.ropeActive);
        buf.writeDouble(msg.ropeRemaining);
        buf.writeBoolean(msg.recalling);
        buf.writeBoolean(msg.pendingLandingRoll);
        buf.writeVarInt(msg.downedTicks);
        buf.writeVarInt(msg.selfRescueTicks);
        buf.writeVarInt(msg.selfRescueRequiredTicks);
    }

    public static S2C_SyncTempestState decode(FriendlyByteBuf buf) {
        return new S2C_SyncTempestState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readDouble(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncTempestState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientTempestHudState.sync(
                        msg.wallCharges,
                        msg.wallMaxCharges,
                        msg.wallRechargeTicks,
                        msg.rollCooldownTicks,
                        msg.coreCooldownTicks,
                        msg.equippedToolOrdinal,
                        msg.ropeActive,
                        msg.ropeRemaining,
                        msg.recalling,
                        msg.pendingLandingRoll,
                        msg.downedTicks,
                        msg.selfRescueTicks,
                        msg.selfRescueRequiredTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
