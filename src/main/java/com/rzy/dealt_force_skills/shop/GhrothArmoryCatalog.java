package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.ghroth.GhrothTaczEnhancement;
import com.rzy.dealt_force_skills.config.DealtForceShopConfig;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem.Faction;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class GhrothArmoryCatalog {
    private static final String DFS_PREFIX = "dfs:";

    private GhrothArmoryCatalog() {
    }

    public static List<Entry> entries() {
        List<Entry> entries = new ArrayList<>();
        addDfsEntries(entries);
        addTaczEntries(entries);
        return entries.stream()
                .sorted(Comparator.comparing(Entry::categoryKey).thenComparingInt(Entry::price).thenComparing(Entry::id))
                .toList();
    }

    public static Optional<Entry> find(String id) {
        return entries().stream().filter(entry -> entry.id().equals(id)).findFirst();
    }

    private static void addDfsEntries(List<Entry> entries) {
        for (DfsShopCatalog.Entry entry : DfsShopCatalog.baseEntriesForArmory()) {
            if (entry.category() == DfsShopCatalog.Category.SPECIAL
                    || entry.category() == DfsShopCatalog.Category.TACZ_GUNS
                    || entry.category() == DfsShopCatalog.Category.TACZ_ATTACHMENTS
                    || entry.category() == DfsShopCatalog.Category.TACZ_AMMO) {
                continue;
            }
            ItemStack stack = entry.preview().copy();
            if ((entry.category() == DfsShopCatalog.Category.HELMET || entry.category() == DfsShopCatalog.Category.ARMOR)
                    && !isHvkEquipment(stack)) {
                continue;
            }
            int price = DealtForceShopConfig.intValue("ghroth_armory.dfs." + entry.id() + ".price",
                    discounted(entry.price()));
            boolean enabled = DealtForceShopConfig.booleanValue("ghroth_armory.dfs." + entry.id() + ".enabled", true);
            if (!enabled) {
                continue;
            }
            entries.add(new Entry(
                    DFS_PREFIX + entry.id(),
                    entry.category().key(),
                    price,
                    stack
            ));
        }
    }

    private static boolean isHvkEquipment(ItemStack stack) {
        DfsEquipmentItem.Profile profile = DfsEquipmentItem.profile(stack);
        return profile != null && profile.faction() == Faction.HVK;
    }

    private static int discounted(int price) {
        double multiplier = DealtForceShopConfig.doubleValue("ghroth_armory.dfs.discount_multiplier", 0.75D);
        return Math.max(1, (int) Math.ceil(price * multiplier));
    }

    private static void addTaczEntries(List<Entry> entries) {
        for (TaczShopCatalog.Entry tacz : TaczShopCatalog.entries()) {
            int price = taczPrice(tacz);
            if (!taczEnabled(tacz)) {
                continue;
            }
            ItemStack stack = tacz.preview().copy();
            if (tacz.type() == TaczShopCatalog.Type.GUN) {
                GhrothTaczEnhancement.markArmoryStack(stack, "gun");
            } else if (tacz.type() == TaczShopCatalog.Type.ATTACHMENT) {
                GhrothTaczEnhancement.markArmoryStack(stack, "attachment");
            }
            entries.add(new Entry(tacz.entryId(), tacz.type().categoryKey(), price, stack));
        }
    }

    public static ItemStack buildPurchasedStack(String entryId) {
        Optional<Entry> found = find(entryId);
        if (found.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return found.get().preview().copy();
    }

    private static int taczPrice(TaczShopCatalog.Entry entry) {
        int sort = entry.sort();
        boolean addon = !"tacz".equals(entry.id().getNamespace());
        int calculated;
        if (entry.type() == TaczShopCatalog.Type.GUN) {
            int base = GhrothTaczEnhancement.isLikelySniper(entry.id()) ? 180_000 : 85_000;
            calculated = Math.min(2_000_000, base + (addon ? 25_000 : 0) + sort * 8);
        } else if (entry.type() == TaczShopCatalog.Type.ATTACHMENT) {
            calculated = Math.min(800_000, 35_000 + (addon ? 10_000 : 0) + sort * 4);
        } else {
            calculated = Math.min(80_000, 2_500 + (addon ? 500 : 0) + sort);
        }
        return DealtForceShopConfig.intValue("ghroth_armory.tacz." + entry.type().configSection()
                + "." + TaczShopCatalog.configKey(entry.id()) + ".price", calculated);
    }

    private static boolean taczEnabled(TaczShopCatalog.Entry entry) {
        return DealtForceShopConfig.booleanValue("ghroth_armory.tacz." + entry.type().configSection()
                + "." + TaczShopCatalog.configKey(entry.id()) + ".enabled", true);
    }

    public record Entry(String id, String categoryKey, int price, ItemStack preview) {
    }
}
