package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.chamber.ChamberMarker;
import com.rzy.dealt_force_skills.character.chamber.ChamberMarkerType;
import com.rzy.dealt_force_skills.client.character.ClientChamberHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_SyncChamberState {
    private final int teleportCooldownTicks;
    private final int trapCooldownTicks;
    private final int headhunterCooldownTicks;
    private final int tourDeForceCooldownTicks;
    private final int equippedToolOrdinal;
    private final int equippedGunOrdinal;
    private final List<ChamberMarker> markers;

    public S2C_SyncChamberState(
            int teleportCooldownTicks,
            int trapCooldownTicks,
            int headhunterCooldownTicks,
            int tourDeForceCooldownTicks,
            int equippedToolOrdinal,
            int equippedGunOrdinal,
            List<ChamberMarker> markers
    ) {
        this.teleportCooldownTicks = teleportCooldownTicks;
        this.trapCooldownTicks = trapCooldownTicks;
        this.headhunterCooldownTicks = headhunterCooldownTicks;
        this.tourDeForceCooldownTicks = tourDeForceCooldownTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.equippedGunOrdinal = equippedGunOrdinal;
        this.markers = List.copyOf(markers);
    }

    public static void encode(S2C_SyncChamberState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.teleportCooldownTicks);
        buf.writeVarInt(msg.trapCooldownTicks);
        buf.writeVarInt(msg.headhunterCooldownTicks);
        buf.writeVarInt(msg.tourDeForceCooldownTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeVarInt(msg.equippedGunOrdinal);
        buf.writeVarInt(msg.markers.size());
        for (ChamberMarker marker : msg.markers) {
            buf.writeVarInt(marker.entityId());
            buf.writeVarInt(marker.type().ordinal());
            buf.writeDouble(marker.position().x);
            buf.writeDouble(marker.position().y);
            buf.writeDouble(marker.position().z);
        }
    }

    public static S2C_SyncChamberState decode(FriendlyByteBuf buf) {
        int teleportCooldownTicks = buf.readVarInt();
        int trapCooldownTicks = buf.readVarInt();
        int headhunterCooldownTicks = buf.readVarInt();
        int tourDeForceCooldownTicks = buf.readVarInt();
        int equippedToolOrdinal = buf.readVarInt();
        int equippedGunOrdinal = buf.readVarInt();
        int count = buf.readVarInt();
        List<ChamberMarker> markers = new ArrayList<>();
        ChamberMarkerType[] types = ChamberMarkerType.values();
        for (int i = 0; i < count; i++) {
            int entityId = buf.readVarInt();
            int typeOrdinal = buf.readVarInt();
            ChamberMarkerType type = typeOrdinal >= 0 && typeOrdinal < types.length
                    ? types[typeOrdinal]
                    : ChamberMarkerType.TELEPORT_ANCHOR;
            markers.add(new ChamberMarker(entityId, type, new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())));
        }
        return new S2C_SyncChamberState(teleportCooldownTicks, trapCooldownTicks, headhunterCooldownTicks,
                tourDeForceCooldownTicks, equippedToolOrdinal, equippedGunOrdinal, markers);
    }

    public static void handle(S2C_SyncChamberState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientChamberHudState.sync(
                        msg.teleportCooldownTicks,
                        msg.trapCooldownTicks,
                        msg.headhunterCooldownTicks,
                        msg.tourDeForceCooldownTicks,
                        msg.equippedToolOrdinal,
                        msg.equippedGunOrdinal,
                        msg.markers
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
