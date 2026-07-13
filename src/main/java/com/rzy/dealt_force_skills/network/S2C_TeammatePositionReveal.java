package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.ClientTeammateRevealState;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client: expose online teammates to the local viewer only.
 * Payload is entity ids + packed team outline RGB (0xRRGGBB). Enemies never receive
 * another team's list, so this never reveals allies to opponents.
 */
public class S2C_TeammatePositionReveal {
    private final List<Entry> entries;
    private final int teamColorRgb;

    public S2C_TeammatePositionReveal(List<Entry> entries, int teamColorRgb) {
        this.entries = List.copyOf(entries);
        this.teamColorRgb = teamColorRgb;
    }

    public static S2C_TeammatePositionReveal clear() {
        return new S2C_TeammatePositionReveal(List.of(), 0x55FFFF);
    }

    public static void encode(S2C_TeammatePositionReveal msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.teamColorRgb);
        buf.writeVarInt(msg.entries.size());
        for (Entry entry : msg.entries) {
            buf.writeVarInt(entry.entityId);
        }
    }

    public static S2C_TeammatePositionReveal decode(FriendlyByteBuf buf) {
        int color = buf.readVarInt();
        int count = buf.readVarInt();
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buf.readVarInt()));
        }
        return new S2C_TeammatePositionReveal(entries, color);
    }

    public static void handle(S2C_TeammatePositionReveal msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> {
                    List<Integer> ids = new ArrayList<>(msg.entries.size());
                    for (Entry entry : msg.entries) {
                        ids.add(entry.entityId);
                    }
                    ClientTeammateRevealState.apply(ids, msg.teamColorRgb);
                }
        ));
        ctx.get().setPacketHandled(true);
    }

    public record Entry(int entityId) {
    }
}
