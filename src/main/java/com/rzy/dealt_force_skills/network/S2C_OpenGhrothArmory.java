package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.GhrothArmoryScreen;
import com.rzy.dealt_force_skills.shop.GhrothArmoryCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_OpenGhrothArmory {
    private final long coins;
    private final List<GhrothArmoryCatalog.Entry> entries;

    public S2C_OpenGhrothArmory(long coins, List<GhrothArmoryCatalog.Entry> entries) {
        this.coins = Math.max(0L, coins);
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static void encode(S2C_OpenGhrothArmory msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.coins);
        buf.writeVarInt(msg.entries.size());
        for (GhrothArmoryCatalog.Entry entry : msg.entries) {
            buf.writeUtf(entry.id());
            buf.writeUtf(entry.categoryKey());
            buf.writeVarInt(Math.max(0, entry.price()));
            buf.writeItem(entry.preview());
        }
    }

    public static S2C_OpenGhrothArmory decode(FriendlyByteBuf buf) {
        long coins = buf.readLong();
        int size = Math.max(0, buf.readVarInt());
        List<GhrothArmoryCatalog.Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String id = buf.readUtf();
            String category = buf.readUtf();
            int price = buf.readVarInt();
            ItemStack preview = buf.readItem();
            entries.add(new GhrothArmoryCatalog.Entry(id, category, price, preview));
        }
        return new S2C_OpenGhrothArmory(coins, entries);
    }

    public static void handle(S2C_OpenGhrothArmory msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> GhrothArmoryScreen.open(msg.coins, msg.entries)
        ));
        ctx.get().setPacketHandled(true);
    }
}
