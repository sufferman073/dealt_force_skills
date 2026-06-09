package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientUluruHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncUluruState {
    private final int incendiaryCharges;
    private final int incendiaryMaxCharges;
    private final int incendiaryRechargeTicks;
    private final int coverCharges;
    private final int coverMaxCharges;
    private final int coverRechargeTicks;
    private final int missileCooldownTicks;
    private final int equippedToolOrdinal;
    private final boolean coverPerpendicular;

    public S2C_SyncUluruState(
            int incendiaryCharges,
            int incendiaryMaxCharges,
            int incendiaryRechargeTicks,
            int coverCharges,
            int coverMaxCharges,
            int coverRechargeTicks,
            int missileCooldownTicks,
            int equippedToolOrdinal,
            boolean coverPerpendicular
    ) {
        this.incendiaryCharges = incendiaryCharges;
        this.incendiaryMaxCharges = incendiaryMaxCharges;
        this.incendiaryRechargeTicks = incendiaryRechargeTicks;
        this.coverCharges = coverCharges;
        this.coverMaxCharges = coverMaxCharges;
        this.coverRechargeTicks = coverRechargeTicks;
        this.missileCooldownTicks = missileCooldownTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.coverPerpendicular = coverPerpendicular;
    }

    public static void encode(S2C_SyncUluruState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.incendiaryCharges);
        buf.writeVarInt(msg.incendiaryMaxCharges);
        buf.writeVarInt(msg.incendiaryRechargeTicks);
        buf.writeVarInt(msg.coverCharges);
        buf.writeVarInt(msg.coverMaxCharges);
        buf.writeVarInt(msg.coverRechargeTicks);
        buf.writeVarInt(msg.missileCooldownTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeBoolean(msg.coverPerpendicular);
    }

    public static S2C_SyncUluruState decode(FriendlyByteBuf buf) {
        return new S2C_SyncUluruState(
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

    public static void handle(S2C_SyncUluruState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientUluruHudState.sync(
                        msg.incendiaryCharges,
                        msg.incendiaryMaxCharges,
                        msg.incendiaryRechargeTicks,
                        msg.coverCharges,
                        msg.coverMaxCharges,
                        msg.coverRechargeTicks,
                        msg.missileCooldownTicks,
                        msg.equippedToolOrdinal,
                        msg.coverPerpendicular
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
