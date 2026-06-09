package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.character.ClientManbaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2C_ManbaFlashlightProgress {
    private final int entityId;
    private final int progressPercent;
    private final int ticks;

    public S2C_ManbaFlashlightProgress(int entityId, int progressPercent, int ticks) {
        this.entityId = entityId;
        this.progressPercent = progressPercent;
        this.ticks = ticks;
    }

    public static void encode(S2C_ManbaFlashlightProgress msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.progressPercent);
        buf.writeVarInt(msg.ticks);
    }

    public static S2C_ManbaFlashlightProgress decode(FriendlyByteBuf buf) {
        return new S2C_ManbaFlashlightProgress(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(S2C_ManbaFlashlightProgress msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientManbaHudState.updateFlashlightProgress(msg.entityId, msg.progressPercent, msg.ticks)
        ));
        ctx.get().setPacketHandled(true);
    }
}
