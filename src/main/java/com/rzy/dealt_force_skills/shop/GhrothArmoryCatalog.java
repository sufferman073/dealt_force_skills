package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.ghroth.GhrothTaczEnhancement;
import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem.Faction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class GhrothArmoryCatalog {
    private static final String DFS_PREFIX = "dfs:";
    private static final String TACZ_GUN_PREFIX = "tacz_gun:";
    private static final String TACZ_ATTACHMENT_PREFIX = "tacz_attachment:";
    private static final String TACZ_AMMO_PREFIX = "tacz_ammo:";

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
        for (DfsShopCatalog.Entry entry : DfsShopCatalog.entries()) {
            if (entry.category() == DfsShopCatalog.Category.SPECIAL) {
                continue;
            }
            ItemStack stack = new ItemStack(entry.item().get());
            if ((entry.category() == DfsShopCatalog.Category.HELMET || entry.category() == DfsShopCatalog.Category.ARMOR)
                    && !isHvkEquipment(stack)) {
                continue;
            }
            entries.add(new Entry(
                    DFS_PREFIX + entry.id(),
                    entry.category().key(),
                    DealtForceConfig.intValue("shop.ghroth_armory.dfs." + entry.id() + ".price",
                            discounted(entry.price())),
                    stack
            ));
        }
    }

    private static boolean isHvkEquipment(ItemStack stack) {
        DfsEquipmentItem.Profile profile = DfsEquipmentItem.profile(stack);
        return profile != null && profile.faction() == Faction.HVK;
    }

    private static int discounted(int price) {
        double multiplier = DealtForceConfig.doubleValue("shop.ghroth_armory.dfs.discount_multiplier", 0.75D);
        return Math.max(1, (int) Math.ceil(price * multiplier));
    }

    private static void addTaczEntries(List<Entry> entries) {
        addTaczIndexEntries(entries, "getAllCommonGunIndex", TACZ_GUN_PREFIX, "tacz_guns",
                "com.tacz.guns.api.item.builder.GunItemBuilder", 1);
        addTaczIndexEntries(entries, "getAllCommonAttachmentIndex", TACZ_ATTACHMENT_PREFIX, "tacz_attachments",
                "com.tacz.guns.api.item.builder.AttachmentItemBuilder", 1);
        addTaczIndexEntries(entries, "getAllCommonAmmoIndex", TACZ_AMMO_PREFIX, "tacz_ammo",
                "com.tacz.guns.api.item.builder.AmmoItemBuilder", 64);
    }

    private static void addTaczIndexEntries(List<Entry> entries, String timelessMethod, String prefix, String category,
                                            String builderClassName, int count) {
        try {
            Class<?> timelessApi = Class.forName("com.tacz.guns.api.TimelessAPI");
            Object result = timelessApi.getMethod(timelessMethod).invoke(null);
            if (!(result instanceof Set<?> set)) {
                return;
            }
            for (Object object : set) {
                if (!(object instanceof Map.Entry<?, ?> indexEntry) || !(indexEntry.getKey() instanceof ResourceLocation id)) {
                    continue;
                }
                ItemStack stack = buildTaczStack(builderClassName, id, count);
                if (stack.isEmpty()) {
                    continue;
                }
                if (TACZ_GUN_PREFIX.equals(prefix)) {
                    GhrothTaczEnhancement.markArmoryStack(stack, "gun");
                } else if (TACZ_ATTACHMENT_PREFIX.equals(prefix)) {
                    GhrothTaczEnhancement.markArmoryStack(stack, "attachment");
                }
                entries.add(new Entry(prefix + id, category, taczPrice(prefix, id, indexEntry.getValue()), stack));
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // TaCZ is mandatory in normal packs, but reflection keeps datagen/build tools tolerant.
        }
    }

    public static ItemStack buildPurchasedStack(String entryId) {
        Optional<Entry> found = find(entryId);
        if (found.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return found.get().preview().copy();
    }

    private static ItemStack buildTaczStack(String builderClassName, ResourceLocation id, int count) {
        try {
            Class<?> builderClass = Class.forName(builderClassName);
            Object builder = builderClass.getMethod("create").invoke(null);
            builder = builderClass.getMethod("setId", ResourceLocation.class).invoke(builder, id);
            if (count > 1) {
                builder = builderClass.getMethod("setCount", int.class).invoke(builder, count);
            }
            Object stack = builderClass.getMethod("build").invoke(builder);
            return stack instanceof ItemStack itemStack ? itemStack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static int taczPrice(String prefix, ResourceLocation id, Object index) {
        int sort = reflectedSort(index);
        boolean addon = !"tacz".equals(id.getNamespace());
        int calculated;
        if (TACZ_GUN_PREFIX.equals(prefix)) {
            int base = GhrothTaczEnhancement.isLikelySniper(id) ? 180_000 : 85_000;
            calculated = Math.min(2_000_000, base + (addon ? 25_000 : 0) + sort * 8);
        } else if (TACZ_ATTACHMENT_PREFIX.equals(prefix)) {
            calculated = Math.min(800_000, 35_000 + (addon ? 10_000 : 0) + sort * 4);
        } else {
            calculated = Math.min(80_000, 2_500 + (addon ? 500 : 0) + sort);
        }
        String type = TACZ_GUN_PREFIX.equals(prefix) ? "guns"
                : TACZ_ATTACHMENT_PREFIX.equals(prefix) ? "attachments" : "ammo";
        String itemKey = (id.getNamespace() + "_" + id.getPath()).replace('/', '_');
        return DealtForceConfig.intValue("shop.ghroth_armory.tacz." + type + "." + itemKey + ".price", calculated);
    }

    private static int reflectedSort(Object index) {
        if (index == null) {
            return 0;
        }
        try {
            Method method = index.getClass().getMethod("getSort");
            Object value = method.invoke(index);
            return value instanceof Number number ? Math.max(0, number.intValue()) : 0;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return 0;
        }
    }

    public record Entry(String id, String categoryKey, int price, ItemStack preview) {
    }
}
