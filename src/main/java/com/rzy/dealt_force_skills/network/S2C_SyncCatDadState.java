package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientCatDadHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncCatDadState {
    private final int hissStage;
    private final int hissTimerTicks;
    private final int hissCooldownTicks;
    private final int blockCharges;
    private final int blockMaxCharges;
    private final int blockRechargeTicks;
    private final int blockWindowTicks;
    private final int coreCooldownTicks;
    private final int downedTicks;
    private final int selfRescueTicks;
    private final int selfRescueRequiredTicks;
    private final int fatalDownedRemainingUses;
    private final int fatalDownedMaxUses;
    private final boolean truckBreakBlocks;

    public S2C_SyncCatDadState(int hissStage, int hissTimerTicks, int hissCooldownTicks,
                               int blockCharges, int blockMaxCharges, int blockRechargeTicks,
                               int blockWindowTicks, int coreCooldownTicks, int downedTicks,
                               int selfRescueTicks, int selfRescueRequiredTicks,
                               int fatalDownedRemainingUses, int fatalDownedMaxUses,
                               boolean truckBreakBlocks) {
        this.hissStage = hissStage;
        this.hissTimerTicks = hissTimerTicks;
        this.hissCooldownTicks = hissCooldownTicks;
        this.blockCharges = blockCharges;
        this.blockMaxCharges = blockMaxCharges;
        this.blockRechargeTicks = blockRechargeTicks;
        this.blockWindowTicks = blockWindowTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.downedTicks = downedTicks;
        this.selfRescueTicks = selfRescueTicks;
        this.selfRescueRequiredTicks = selfRescueRequiredTicks;
        this.fatalDownedRemainingUses = fatalDownedRemainingUses;
        this.fatalDownedMaxUses = fatalDownedMaxUses;
        this.truckBreakBlocks = truckBreakBlocks;
    }

    public static void encode(S2C_SyncCatDadState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.hissStage);
        buf.writeVarInt(msg.hissTimerTicks);
        buf.writeVarInt(msg.hissCooldownTicks);
        buf.writeVarInt(msg.blockCharges);
        buf.writeVarInt(msg.blockMaxCharges);
        buf.writeVarInt(msg.blockRechargeTicks);
        buf.writeVarInt(msg.blockWindowTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.downedTicks);
        buf.writeVarInt(msg.selfRescueTicks);
        buf.writeVarInt(msg.selfRescueRequiredTicks);
        buf.writeVarInt(msg.fatalDownedRemainingUses);
        buf.writeVarInt(msg.fatalDownedMaxUses);
        buf.writeBoolean(msg.truckBreakBlocks);
    }

    public static S2C_SyncCatDadState decode(FriendlyByteBuf buf) {
        return new S2C_SyncCatDadState(
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
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean()
        );
    }

    public static void handle(S2C_SyncCatDadState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientCatDadHudState.sync(
                        msg.hissStage,
                        msg.hissTimerTicks,
                        msg.hissCooldownTicks,
                        msg.blockCharges,
                        msg.blockMaxCharges,
                        msg.blockRechargeTicks,
                        msg.blockWindowTicks,
                        msg.coreCooldownTicks,
                        msg.downedTicks,
                        msg.selfRescueTicks,
                        msg.selfRescueRequiredTicks,
                        msg.fatalDownedRemainingUses,
                        msg.fatalDownedMaxUses,
                        msg.truckBreakBlocks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
