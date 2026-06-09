package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientVyronHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncVyronState {
    private final int dashCooldownTicks;
    private final int dashTicks;
    private final int poweredTicks;
    private final int bombCharges;
    private final int bombMaxCharges;
    private final int bombRechargeTicks;
    private final int coreCooldownTicks;
    private final int equippedToolOrdinal;

    public S2C_SyncVyronState(
            int dashCooldownTicks,
            int dashTicks,
            int poweredTicks,
            int bombCharges,
            int bombMaxCharges,
            int bombRechargeTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal
    ) {
        this.dashCooldownTicks = dashCooldownTicks;
        this.dashTicks = dashTicks;
        this.poweredTicks = poweredTicks;
        this.bombCharges = bombCharges;
        this.bombMaxCharges = bombMaxCharges;
        this.bombRechargeTicks = bombRechargeTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
    }

    public static void encode(S2C_SyncVyronState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.dashCooldownTicks);
        buf.writeVarInt(msg.dashTicks);
        buf.writeVarInt(msg.poweredTicks);
        buf.writeVarInt(msg.bombCharges);
        buf.writeVarInt(msg.bombMaxCharges);
        buf.writeVarInt(msg.bombRechargeTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
    }

    public static S2C_SyncVyronState decode(FriendlyByteBuf buf) {
        return new S2C_SyncVyronState(
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

    public static void handle(S2C_SyncVyronState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientVyronHudState.sync(
                        msg.dashCooldownTicks,
                        msg.dashTicks,
                        msg.poweredTicks,
                        msg.bombCharges,
                        msg.bombMaxCharges,
                        msg.bombRechargeTicks,
                        msg.coreCooldownTicks,
                        msg.equippedToolOrdinal
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
