package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientGhrothState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncGhrothState {
    private final int starsCooldownTicks;
    private final int justiceCooldownTicks;
    private final int noonCooldownTicks;
    private final int starsActiveTicks;
    private final int justiceActiveTicks;
    private final int noonActiveTicks;
    private final int noonGazeTargetId;
    private final int noonGazeTicks;
    private final int noonDamageCopies;
    private final boolean tacticalImmune;

    public S2C_SyncGhrothState(int starsCooldownTicks, int justiceCooldownTicks, int noonCooldownTicks,
                               int starsActiveTicks, int justiceActiveTicks, int noonActiveTicks,
                               int noonGazeTargetId, int noonGazeTicks, int noonDamageCopies,
                               boolean tacticalImmune) {
        this.starsCooldownTicks = Math.max(0, starsCooldownTicks);
        this.justiceCooldownTicks = Math.max(0, justiceCooldownTicks);
        this.noonCooldownTicks = Math.max(0, noonCooldownTicks);
        this.starsActiveTicks = Math.max(0, starsActiveTicks);
        this.justiceActiveTicks = Math.max(0, justiceActiveTicks);
        this.noonActiveTicks = Math.max(0, noonActiveTicks);
        this.noonGazeTargetId = Math.max(0, noonGazeTargetId);
        this.noonGazeTicks = Math.max(0, noonGazeTicks);
        this.noonDamageCopies = Math.max(0, noonDamageCopies);
        this.tacticalImmune = tacticalImmune;
    }

    public static void encode(S2C_SyncGhrothState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.starsCooldownTicks);
        buf.writeVarInt(msg.justiceCooldownTicks);
        buf.writeVarInt(msg.noonCooldownTicks);
        buf.writeVarInt(msg.starsActiveTicks);
        buf.writeVarInt(msg.justiceActiveTicks);
        buf.writeVarInt(msg.noonActiveTicks);
        buf.writeVarInt(msg.noonGazeTargetId);
        buf.writeVarInt(msg.noonGazeTicks);
        buf.writeVarInt(msg.noonDamageCopies);
        buf.writeBoolean(msg.tacticalImmune);
    }

    public static S2C_SyncGhrothState decode(FriendlyByteBuf buf) {
        return new S2C_SyncGhrothState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean()
        );
    }

    public static void handle(S2C_SyncGhrothState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientGhrothState.sync(
                        msg.starsCooldownTicks,
                        msg.justiceCooldownTicks,
                        msg.noonCooldownTicks,
                        msg.starsActiveTicks,
                        msg.justiceActiveTicks,
                        msg.noonActiveTicks,
                        msg.noonGazeTargetId,
                        msg.noonGazeTicks,
                        msg.noonDamageCopies,
                        msg.tacticalImmune
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
