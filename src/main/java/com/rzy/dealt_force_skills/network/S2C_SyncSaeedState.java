package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientSaeedHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncSaeedState {
    private final int fireAmmo;
    private final int fireMaxAmmo;
    private final int fireRechargeTicks;
    private final boolean crossbowEquipped;
    private final boolean fireBounce;
    private final int rollCooldownTicks;
    private final int coreCooldownTicks;
    private final int coreActiveTicks;
    private final int commandVisualTicks;

    public S2C_SyncSaeedState(
            int fireAmmo,
            int fireMaxAmmo,
            int fireRechargeTicks,
            boolean crossbowEquipped,
            boolean fireBounce,
            int rollCooldownTicks,
            int coreCooldownTicks,
            int coreActiveTicks,
            int commandVisualTicks
    ) {
        this.fireAmmo = fireAmmo;
        this.fireMaxAmmo = fireMaxAmmo;
        this.fireRechargeTicks = fireRechargeTicks;
        this.crossbowEquipped = crossbowEquipped;
        this.fireBounce = fireBounce;
        this.rollCooldownTicks = rollCooldownTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.coreActiveTicks = coreActiveTicks;
        this.commandVisualTicks = commandVisualTicks;
    }

    public static void encode(S2C_SyncSaeedState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.fireAmmo);
        buf.writeVarInt(msg.fireMaxAmmo);
        buf.writeVarInt(msg.fireRechargeTicks);
        buf.writeBoolean(msg.crossbowEquipped);
        buf.writeBoolean(msg.fireBounce);
        buf.writeVarInt(msg.rollCooldownTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.coreActiveTicks);
        buf.writeVarInt(msg.commandVisualTicks);
    }

    public static S2C_SyncSaeedState decode(FriendlyByteBuf buf) {
        return new S2C_SyncSaeedState(
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt(),
                buf.readVarInt()
        );
    }

    public static void handle(S2C_SyncSaeedState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSaeedHudState.sync(
                        msg.fireAmmo,
                        msg.fireMaxAmmo,
                        msg.fireRechargeTicks,
                        msg.crossbowEquipped,
                        msg.fireBounce,
                        msg.rollCooldownTicks,
                        msg.coreCooldownTicks,
                        msg.coreActiveTicks,
                        msg.commandVisualTicks
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
