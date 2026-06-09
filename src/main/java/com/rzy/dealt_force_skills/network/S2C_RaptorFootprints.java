package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.raptor.RaptorFootprintMarker;
import com.rzy.dealt_force_skills.client.character.ClientRaptorHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_RaptorFootprints {
    private final List<RaptorFootprintMarker> markers;

    public S2C_RaptorFootprints(List<RaptorFootprintMarker> markers) {
        this.markers = List.copyOf(markers);
    }

    public static void encode(S2C_RaptorFootprints msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.markers.size());
        for (RaptorFootprintMarker marker : msg.markers) {
            buf.writeVarInt(marker.id());
            buf.writeDouble(marker.position().x);
            buf.writeDouble(marker.position().y);
            buf.writeDouble(marker.position().z);
            buf.writeVarInt(marker.ageTicks());
            buf.writeUtf(marker.ownerName(), 64);
            buf.writeUtf(marker.equipmentSummary(), 160);
        }
    }

    public static S2C_RaptorFootprints decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<RaptorFootprintMarker> markers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            markers.add(new RaptorFootprintMarker(
                    buf.readVarInt(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()),
                    buf.readVarInt(),
                    buf.readUtf(64),
                    buf.readUtf(160)
            ));
        }
        return new S2C_RaptorFootprints(markers);
    }

    public static void handle(S2C_RaptorFootprints msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientRaptorHudState.syncFootprints(msg.markers)
        ));
        ctx.get().setPacketHandled(true);
    }
}
