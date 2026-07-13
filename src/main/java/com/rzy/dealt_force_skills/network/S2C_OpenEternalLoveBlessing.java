package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.EternalLoveBlessingScreen;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_OpenEternalLoveBlessing {
    private static final int MAX_ENTRIES = 256;

    private final List<Entry> entries;

    public S2C_OpenEternalLoveBlessing(List<Entry> entries) {
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static void encode(S2C_OpenEternalLoveBlessing msg, FriendlyByteBuf buf) {
        buf.writeVarInt(Math.min(MAX_ENTRIES, msg.entries.size()));
        for (int i = 0; i < msg.entries.size() && i < MAX_ENTRIES; i++) {
            Entry entry = msg.entries.get(i);
            buf.writeUtf(entry.effectId(), 512);
            buf.writeUtf(entry.name(), 256);
            buf.writeVarInt(entry.level());
        }
    }

    public static S2C_OpenEternalLoveBlessing decode(FriendlyByteBuf buf) {
        int count = Math.max(0, Math.min(MAX_ENTRIES, buf.readVarInt()));
        List<Entry> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            entries.add(new Entry(buf.readUtf(512), buf.readUtf(256), buf.readVarInt()));
        }
        return new S2C_OpenEternalLoveBlessing(entries);
    }

    public static void handle(S2C_OpenEternalLoveBlessing msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> EternalLoveBlessingScreen.open(msg.entries)
        ));
        ctx.get().setPacketHandled(true);
    }

    public record Entry(String effectId, String name, int level) {
    }
}
