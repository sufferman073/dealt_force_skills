package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.block.AdvancedWorkBlockData;
import com.rzy.dealt_force_skills.block.AdvancedWorkBlockData.EntryView;
import com.rzy.dealt_force_skills.client.screen.AdvancedWorkBlockScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_OpenAdvancedWorkBlock {
    private static final int MAX_ENTRIES = 128;
    private static final int MAX_OUTPUTS = 64;

    private final AdvancedWorkBlockData data;

    public S2C_OpenAdvancedWorkBlock(AdvancedWorkBlockData data) {
        this.data = data;
    }

    public static void encode(S2C_OpenAdvancedWorkBlock msg, FriendlyByteBuf buf) {
        writeData(msg.data, buf);
    }

    public static S2C_OpenAdvancedWorkBlock decode(FriendlyByteBuf buf) {
        return new S2C_OpenAdvancedWorkBlock(readData(buf));
    }

    public static void handle(S2C_OpenAdvancedWorkBlock msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> AdvancedWorkBlockScreen.open(msg.data)
        ));
        ctx.get().setPacketHandled(true);
    }

    static void writeData(AdvancedWorkBlockData data, FriendlyByteBuf buf) {
        AdvancedWorkBlockData safe = data == null
                ? new AdvancedWorkBlockData(BlockPos.ZERO, 0, 0, "", 0, 0, 0.0D, List.of())
                : data;
        buf.writeBlockPos(safe.pos());
        buf.writeVarInt(safe.blockType());
        buf.writeVarInt(safe.mode());
        buf.writeUtf(safe.query());
        buf.writeVarInt(safe.page());
        buf.writeVarInt(safe.total());
        buf.writeDouble(safe.storedEnergy());
        buf.writeVarInt(Math.min(MAX_ENTRIES, safe.entries().size()));
        for (int i = 0; i < safe.entries().size() && i < MAX_ENTRIES; i++) {
            EntryView entry = safe.entries().get(i);
            buf.writeUtf(entry.id() == null ? "minecraft:air" : entry.id().toString());
            buf.writeItem(entry.icon());
            buf.writeUtf(entry.name(), 256);
            buf.writeUtf(entry.detail(), 256);
            buf.writeVarInt(entry.available());
            buf.writeDouble(entry.cost());
            buf.writeVarInt(Math.min(MAX_OUTPUTS, entry.outputs().size()));
            for (int j = 0; j < entry.outputs().size() && j < MAX_OUTPUTS; j++) {
                buf.writeItem(entry.outputs().get(j));
            }
        }
    }

    static AdvancedWorkBlockData readData(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int blockType = buf.readVarInt();
        int mode = buf.readVarInt();
        String query = buf.readUtf(128);
        int page = buf.readVarInt();
        int total = buf.readVarInt();
        double storedEnergy = buf.readDouble();
        int count = Math.max(0, Math.min(MAX_ENTRIES, buf.readVarInt()));
        List<EntryView> entries = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            ResourceLocation id = readResourceLocation(buf);
            ItemStack icon = buf.readItem();
            String name = buf.readUtf(256);
            String detail = buf.readUtf(256);
            int available = buf.readVarInt();
            double cost = buf.readDouble();
            int outputCount = Math.max(0, Math.min(MAX_OUTPUTS, buf.readVarInt()));
            List<ItemStack> outputs = new ArrayList<>(outputCount);
            for (int j = 0; j < outputCount; j++) {
                outputs.add(buf.readItem());
            }
            entries.add(new EntryView(id, icon, name, detail, outputs, available, cost));
        }
        return new AdvancedWorkBlockData(pos, blockType, mode, query, page, total, storedEnergy, entries);
    }

    static ResourceLocation readResourceLocation(FriendlyByteBuf buf) {
        String value = buf.readUtf(512);
        try {
            return ResourceLocation.parse(value);
        } catch (RuntimeException ignored) {
            return ResourceLocation.fromNamespaceAndPath("minecraft", "air");
        }
    }
}
