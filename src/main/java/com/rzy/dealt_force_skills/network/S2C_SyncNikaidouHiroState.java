package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientNikaidouHiroHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncNikaidouHiroState {
    private final int riftStacks;
    private final int riftTicks;
    private final int active1CooldownTicks;
    private final int active1Ticks;
    private final int equippedToolOrdinal;
    private final int coreCooldownTicks;
    private final boolean coreActive;
    private final int coreActiveTicks;
    private final int doomedTicks;
    private final int attackCooldownTicks;
    private final boolean anchorMarkerActive;
    private final String anchorMarkerDimension;
    private final double anchorMarkerX;
    private final double anchorMarkerY;
    private final double anchorMarkerZ;

    public S2C_SyncNikaidouHiroState(int riftStacks, int riftTicks, int active1CooldownTicks, int active1Ticks,
                                     int equippedToolOrdinal, int coreCooldownTicks, boolean coreActive,
                                     int coreActiveTicks, int doomedTicks, int attackCooldownTicks) {
        this(riftStacks, riftTicks, active1CooldownTicks, active1Ticks, equippedToolOrdinal, coreCooldownTicks,
                coreActive, coreActiveTicks, doomedTicks, attackCooldownTicks, false, "", 0.0D, 0.0D, 0.0D);
    }

    public S2C_SyncNikaidouHiroState(int riftStacks, int riftTicks, int active1CooldownTicks, int active1Ticks,
                                     int equippedToolOrdinal, int coreCooldownTicks, boolean coreActive,
                                     int coreActiveTicks, int doomedTicks, int attackCooldownTicks,
                                     boolean anchorMarkerActive, String anchorMarkerDimension,
                                     double anchorMarkerX, double anchorMarkerY, double anchorMarkerZ) {
        this.riftStacks = riftStacks;
        this.riftTicks = riftTicks;
        this.active1CooldownTicks = active1CooldownTicks;
        this.active1Ticks = active1Ticks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.coreCooldownTicks = coreCooldownTicks;
        this.coreActive = coreActive;
        this.coreActiveTicks = coreActiveTicks;
        this.doomedTicks = doomedTicks;
        this.attackCooldownTicks = attackCooldownTicks;
        this.anchorMarkerActive = anchorMarkerActive;
        this.anchorMarkerDimension = anchorMarkerDimension == null ? "" : anchorMarkerDimension;
        this.anchorMarkerX = anchorMarkerX;
        this.anchorMarkerY = anchorMarkerY;
        this.anchorMarkerZ = anchorMarkerZ;
    }

    public static void encode(S2C_SyncNikaidouHiroState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.riftStacks);
        buf.writeVarInt(msg.riftTicks);
        buf.writeVarInt(msg.active1CooldownTicks);
        buf.writeVarInt(msg.active1Ticks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeBoolean(msg.coreActive);
        buf.writeVarInt(msg.coreActiveTicks);
        buf.writeVarInt(msg.doomedTicks);
        buf.writeVarInt(msg.attackCooldownTicks);
        buf.writeBoolean(msg.anchorMarkerActive);
        buf.writeUtf(msg.anchorMarkerDimension);
        buf.writeDouble(msg.anchorMarkerX);
        buf.writeDouble(msg.anchorMarkerY);
        buf.writeDouble(msg.anchorMarkerZ);
    }

    public static S2C_SyncNikaidouHiroState decode(FriendlyByteBuf buf) {
        return new S2C_SyncNikaidouHiroState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readUtf(32767),
                buf.readDouble(),
                buf.readDouble(),
                buf.readDouble()
        );
    }

    public static void handle(S2C_SyncNikaidouHiroState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientNikaidouHiroHudState.sync(
                        msg.riftStacks,
                        msg.riftTicks,
                        msg.active1CooldownTicks,
                        msg.active1Ticks,
                        msg.equippedToolOrdinal,
                        msg.coreCooldownTicks,
                        msg.coreActive,
                        msg.coreActiveTicks,
                        msg.doomedTicks,
                        msg.attackCooldownTicks,
                        msg.anchorMarkerActive,
                        msg.anchorMarkerDimension,
                        msg.anchorMarkerX,
                        msg.anchorMarkerY,
                        msg.anchorMarkerZ
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
