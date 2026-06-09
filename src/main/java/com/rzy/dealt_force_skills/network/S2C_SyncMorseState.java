package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientMorseHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncMorseState {
    private final int shockCharges;
    private final int shockMaxCharges;
    private final int shockRechargeTicks;
    private final int flashCharges;
    private final int flashMaxCharges;
    private final int flashRechargeTicks;
    private final int sonarCooldownTicks;
    private final int equippedToolOrdinal;
    private final int deployTicks;
    private final int deployRequiredTicks;
    private final boolean sonarActive;
    private final boolean sonarScanning;
    private final int sonarRemainingTicks;
    private final int sonarTargetCount;

    public S2C_SyncMorseState(
            int shockCharges,
            int shockMaxCharges,
            int shockRechargeTicks,
            int flashCharges,
            int flashMaxCharges,
            int flashRechargeTicks,
            int sonarCooldownTicks,
            int equippedToolOrdinal,
            int deployTicks,
            int deployRequiredTicks,
            boolean sonarActive,
            boolean sonarScanning,
            int sonarRemainingTicks,
            int sonarTargetCount
    ) {
        this.shockCharges = shockCharges;
        this.shockMaxCharges = shockMaxCharges;
        this.shockRechargeTicks = shockRechargeTicks;
        this.flashCharges = flashCharges;
        this.flashMaxCharges = flashMaxCharges;
        this.flashRechargeTicks = flashRechargeTicks;
        this.sonarCooldownTicks = sonarCooldownTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.deployTicks = deployTicks;
        this.deployRequiredTicks = deployRequiredTicks;
        this.sonarActive = sonarActive;
        this.sonarScanning = sonarScanning;
        this.sonarRemainingTicks = sonarRemainingTicks;
        this.sonarTargetCount = sonarTargetCount;
    }

    public static void encode(S2C_SyncMorseState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.shockCharges);
        buf.writeVarInt(msg.shockMaxCharges);
        buf.writeVarInt(msg.shockRechargeTicks);
        buf.writeVarInt(msg.flashCharges);
        buf.writeVarInt(msg.flashMaxCharges);
        buf.writeVarInt(msg.flashRechargeTicks);
        buf.writeVarInt(msg.sonarCooldownTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeVarInt(msg.deployTicks);
        buf.writeVarInt(msg.deployRequiredTicks);
        buf.writeBoolean(msg.sonarActive);
        buf.writeBoolean(msg.sonarScanning);
        buf.writeVarInt(msg.sonarRemainingTicks);
        buf.writeVarInt(msg.sonarTargetCount);
    }

    public static S2C_SyncMorseState decode(FriendlyByteBuf buf) {
        return new S2C_SyncMorseState(
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
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncMorseState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientMorseHudState.sync(
                        msg.shockCharges,
                        msg.shockMaxCharges,
                        msg.shockRechargeTicks,
                        msg.flashCharges,
                        msg.flashMaxCharges,
                        msg.flashRechargeTicks,
                        msg.sonarCooldownTicks,
                        msg.equippedToolOrdinal,
                        msg.deployTicks,
                        msg.deployRequiredTicks,
                        msg.sonarActive,
                        msg.sonarScanning,
                        msg.sonarRemainingTicks,
                        msg.sonarTargetCount
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
