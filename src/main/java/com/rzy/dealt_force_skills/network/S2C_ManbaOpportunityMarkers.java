package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.manba.ManbaOpportunityMarker;
import com.rzy.dealt_force_skills.character.manba.ManbaOpportunityMarkerType;
import com.rzy.dealt_force_skills.client.character.ClientManbaHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_ManbaOpportunityMarkers {
    private final List<ManbaOpportunityMarker> markers;

    public S2C_ManbaOpportunityMarkers(List<ManbaOpportunityMarker> markers) {
        this.markers = markers == null ? List.of() : List.copyOf(markers);
    }

    public static void encode(S2C_ManbaOpportunityMarkers msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.markers.size());
        for (ManbaOpportunityMarker marker : msg.markers) {
            buf.writeVarInt(marker.type().ordinal());
            buf.writeDouble(marker.position().x);
            buf.writeDouble(marker.position().y);
            buf.writeDouble(marker.position().z);
            buf.writeUtf(marker.label(), 64);
        }
    }

    public static S2C_ManbaOpportunityMarkers decode(FriendlyByteBuf buf) {
        int count = buf.readVarInt();
        List<ManbaOpportunityMarker> markers = new ArrayList<>();
        ManbaOpportunityMarkerType[] types = ManbaOpportunityMarkerType.values();
        for (int i = 0; i < count; i++) {
            int typeOrdinal = buf.readVarInt();
            ManbaOpportunityMarkerType type = typeOrdinal >= 0 && typeOrdinal < types.length
                    ? types[typeOrdinal]
                    : ManbaOpportunityMarkerType.OPENED_OR_NORMAL;
            Vec3 position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            String label = buf.readUtf(64);
            markers.add(new ManbaOpportunityMarker(type, position, label));
        }
        return new S2C_ManbaOpportunityMarkers(markers);
    }

    public static void handle(S2C_ManbaOpportunityMarkers msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientManbaHudState.syncOpportunityMarkers(msg.markers)
        ));
        ctx.get().setPacketHandled(true);
    }
}
