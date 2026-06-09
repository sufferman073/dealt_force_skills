package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientSinevaRenderState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_SyncSinevaRenderState {
    private final int entityId;
    private final boolean sineva;
    private final boolean bombSuitActive;
    private final boolean shieldDeployed;
    private final boolean viewportBroken;

    public S2C_SyncSinevaRenderState(int entityId, boolean sineva, boolean bombSuitActive, boolean shieldDeployed, boolean viewportBroken) {
        this.entityId = entityId;
        this.sineva = sineva;
        this.bombSuitActive = bombSuitActive;
        this.shieldDeployed = shieldDeployed;
        this.viewportBroken = viewportBroken;
    }

    public static void encode(S2C_SyncSinevaRenderState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeBoolean(msg.sineva);
        buf.writeBoolean(msg.bombSuitActive);
        buf.writeBoolean(msg.shieldDeployed);
        buf.writeBoolean(msg.viewportBroken);
    }

    public static S2C_SyncSinevaRenderState decode(FriendlyByteBuf buf) {
        return new S2C_SyncSinevaRenderState(
                buf.readVarInt(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean(),
                buf.readBoolean()
        );
    }

    public static void handle(S2C_SyncSinevaRenderState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientSinevaRenderState.sync(
                        msg.entityId,
                        msg.sineva,
                        msg.bombSuitActive,
                        msg.shieldDeployed,
                        msg.viewportBroken
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
