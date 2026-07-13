package com.rzy.dealt_force_skills.network;

import com.rzy.dealt_force_skills.client.screen.HaffShopScreen;
import com.rzy.dealt_force_skills.item.DfsItemQuality;
import com.rzy.dealt_force_skills.shop.DfsShopCatalog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class S2C_OpenHaffShop {
    private final long coins;
    private final List<DfsShopCatalog.Entry> entries;

    public S2C_OpenHaffShop(long coins, List<DfsShopCatalog.Entry> entries) {
        this.coins = Math.max(0L, coins);
        this.entries = entries == null ? List.of() : List.copyOf(entries);
    }

    public static void encode(S2C_OpenHaffShop msg, FriendlyByteBuf buf) {
        buf.writeLong(msg.coins);
        buf.writeVarInt(msg.entries.size());
        for (DfsShopCatalog.Entry entry : msg.entries) {
            buf.writeUtf(entry.id());
            buf.writeUtf(entry.category().key());
            buf.writeUtf(entry.quality().key());
            buf.writeVarInt(Math.max(0, entry.price()));
            buf.writeItem(entry.preview());
        }
    }

    public static S2C_OpenHaffShop decode(FriendlyByteBuf buf) {
        long coins = buf.readLong();
        int size = Math.max(0, buf.readVarInt());
        List<DfsShopCatalog.Entry> entries = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            String id = buf.readUtf();
            DfsShopCatalog.Category category = DfsShopCatalog.Category.byKey(buf.readUtf());
            DfsItemQuality quality = qualityByKey(buf.readUtf());
            int price = buf.readVarInt();
            ItemStack preview = buf.readItem();
            entries.add(new DfsShopCatalog.Entry(id, category, quality, price, preview));
        }
        return new S2C_OpenHaffShop(coins, entries);
    }

    public static void handle(S2C_OpenHaffShop msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> DistExecutor.unsafeRunWhenOn(
                Dist.CLIENT,
                () -> () -> HaffShopScreen.open(msg.coins, msg.entries)
        ));
        ctx.get().setPacketHandled(true);
    }

    private static DfsItemQuality qualityByKey(String key) {
        for (DfsItemQuality quality : DfsItemQuality.values()) {
            if (quality.key().equals(key)) {
                return quality;
            }
        }
        return DfsItemQuality.WHITE;
    }
}
