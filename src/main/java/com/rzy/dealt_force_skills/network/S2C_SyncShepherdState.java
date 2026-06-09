package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.character.shepherd.ShepherdTrapMarker;
import com.rzy.dealt_force_skills.client.character.ClientShepherdHudState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_SyncShepherdState {
    private final int trapCharges;
    private final int trapMaxCharges;
    private final int trapRechargeTicks;
    private final int fragCharges;
    private final int fragMaxCharges;
    private final int fragRechargeTicks;
    private final int coreCooldownTicks;
    private final int equippedToolOrdinal;
    private final int grenadeCookTicks;
    private final List<ShepherdTrapMarker> trapMarkers;

    public S2C_SyncShepherdState(
            int trapCharges,
            int trapMaxCharges,
            int trapRechargeTicks,
            int fragCharges,
            int fragMaxCharges,
            int fragRechargeTicks,
            int coreCooldownTicks,
            int equippedToolOrdinal,
            int grenadeCookTicks,
            List<ShepherdTrapMarker> trapMarkers
    ) {
        this.trapCharges = trapCharges;
        this.trapMaxCharges = trapMaxCharges;
        this.trapRechargeTicks = trapRechargeTicks;
        this.fragCharges = fragCharges;
        this.fragMaxCharges = fragMaxCharges;
        this.fragRechargeTicks = fragRechargeTicks;
        this.coreCooldownTicks = coreCooldownTicks;
        this.equippedToolOrdinal = equippedToolOrdinal;
        this.grenadeCookTicks = grenadeCookTicks;
        this.trapMarkers = List.copyOf(trapMarkers);
    }

    public static void encode(S2C_SyncShepherdState msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.trapCharges);
        buf.writeVarInt(msg.trapMaxCharges);
        buf.writeVarInt(msg.trapRechargeTicks);
        buf.writeVarInt(msg.fragCharges);
        buf.writeVarInt(msg.fragMaxCharges);
        buf.writeVarInt(msg.fragRechargeTicks);
        buf.writeVarInt(msg.coreCooldownTicks);
        buf.writeVarInt(msg.equippedToolOrdinal);
        buf.writeVarInt(msg.grenadeCookTicks);
        buf.writeVarInt(msg.trapMarkers.size());
        for (ShepherdTrapMarker marker : msg.trapMarkers) {
            buf.writeVarInt(marker.entityId());
            buf.writeDouble(marker.position().x);
            buf.writeDouble(marker.position().y);
            buf.writeDouble(marker.position().z);
        }
    }

    public static S2C_SyncShepherdState decode(FriendlyByteBuf buf) {
        int trapCharges = buf.readVarInt();
        int trapMaxCharges = buf.readVarInt();
        int trapRechargeTicks = buf.readVarInt();
        int fragCharges = buf.readVarInt();
        int fragMaxCharges = buf.readVarInt();
        int fragRechargeTicks = buf.readVarInt();
        int coreCooldownTicks = buf.readVarInt();
        int equippedToolOrdinal = buf.readVarInt();
        int grenadeCookTicks = buf.readVarInt();
        int count = buf.readVarInt();
        List<ShepherdTrapMarker> markers = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            markers.add(new ShepherdTrapMarker(buf.readVarInt(),
                    new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble())));
        }
        return new S2C_SyncShepherdState(trapCharges, trapMaxCharges, trapRechargeTicks,
                fragCharges, fragMaxCharges, fragRechargeTicks, coreCooldownTicks,
                equippedToolOrdinal, grenadeCookTicks, markers);
    }

    public static void handle(S2C_SyncShepherdState msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> ClientShepherdHudState.sync(
                        msg.trapCharges,
                        msg.trapMaxCharges,
                        msg.trapRechargeTicks,
                        msg.fragCharges,
                        msg.fragMaxCharges,
                        msg.fragRechargeTicks,
                        msg.coreCooldownTicks,
                        msg.equippedToolOrdinal,
                        msg.grenadeCookTicks,
                        msg.trapMarkers
                )
        ));
        ctx.get().setPacketHandled(true);
    }
}
