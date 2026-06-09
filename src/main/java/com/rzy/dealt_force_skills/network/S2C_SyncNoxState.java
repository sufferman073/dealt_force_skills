package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientNoxHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncNoxState {
    private final int rotorCooldownTicks;
    private final int flashCharges;
    private final int flashMaxCharges;
    private final int flashRechargeTicks;
    private final int coreCooldownTicks;
    private final int corePrepTicks;
    private final int stealthTicks;
    private final int equippedToolOrdinal;

    public S2C_SyncNoxState(
            int rotorCooldownTicks,
            int flashCharges,
            int flashMaxCharges,
            int flashRechargeTicks,
            int coreCooldownTicks,
            int corePrepTicks,
            int stealthTicks,
            int equippedToolOrdinal
    ) {
        this.rotorCooldownTicks = rotorCooldownTicks;
        this.flashCharges = flashCharges;
        this.flashMaxCharges = flashMaxCharges;
        this.flashRechargeTicks = flashRechargeTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.corePrepTicks = corePrepTicks;
        this.stealthTicks = stealthTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
    }

    public static void encode(S2C_SyncNoxState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.rotorCooldownTicks);
        buf.writeVarInt(msg.flashCharges);
        buf.writeVarInt(msg.flashMaxCharges);
        buf.writeVarInt(msg.flashRechargeTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.corePrepTicks);
        buf.writeVarInt(msg.stealthTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
    }

    public static S2C_SyncNoxState decode(FriendlyByteBuf buf) {
        return new S2C_SyncNoxState(
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

    public static void handle(S2C_SyncNoxState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientNoxHudState.sync(
                        msg.rotorCooldownTicks,
                        msg.flashCharges,
                        msg.flashMaxCharges,
                        msg.flashRechargeTicks,
                        msg.coreCooldownTicks,
                        msg.corePrepTicks,
                        msg.stealthTicks,
                        msg.equippedToolOrdinal
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
