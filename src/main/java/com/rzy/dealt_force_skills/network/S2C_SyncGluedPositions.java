package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.HvkGlueClientCache;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Full-replace sync of every position glued (by HVK Universal Glue) in one dimension. Sent on a
 * successful glue (to everyone currently in that dimension) and on player join / dimension change
 * (to bring the client's {@link HvkGlueClientCache} up to date).
 */
public record S2C_SyncGluedPositions(ResourceKey<Level> dimension, long[] positions) {
    public static void encode(S2C_SyncGluedPositions msg, FriendlyByteBuf buf) {
        buf.writeResourceLocation(msg.dimension.location());
        buf.writeVarInt(msg.positions.length);
        for (long pos : msg.positions) {
            buf.writeLong(pos);
        }
    }

    public static S2C_SyncGluedPositions decode(FriendlyByteBuf buf) {
        ResourceKey<Level> dimension = ResourceKey.create(Registries.DIMENSION, buf.readResourceLocation());
        int size = buf.readVarInt();
        long[] positions = new long[size];
        for (int i = 0; i < size; i++) {
            positions[i] = buf.readLong();
        }
        return new S2C_SyncGluedPositions(dimension, positions);
    }

    public static void handle(S2C_SyncGluedPositions msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HvkGlueClientCache.replaceAll(msg.dimension, msg.positions)
        ));
        ctx.get().setPacketHandled(true);
    }
}
