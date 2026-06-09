package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.undead.UndeadProfession;

import java.util.Arrays;
import java.util.Optional;

public enum UndeadShopEntry {
    WEAPON_POLISH("weapon_polish", UndeadShopCategory.SERVICE, 3000, 10, false, null),
    ARMOR_FORGING("armor_forging", UndeadShopCategory.SERVICE, 3000, 10, false, null),
    TALENT_CAPACITY("talent_capacity", UndeadShopCategory.SERVICE, 1200, 55, false, null),
    THOUSAND_TEMPERING("thousand_tempering", UndeadShopCategory.SERVICE, 1500, Integer.MAX_VALUE, false, null),

    RUBY_BRACELET("ruby_bracelet", UndeadShopCategory.BRACELET, 8000, 1, false, null),
    PINK_DIAMOND_BRACELET("pink_diamond_bracelet", UndeadShopCategory.BRACELET, 10000, 1, false, null),
    OPAL_BRACELET("opal_bracelet", UndeadShopCategory.BRACELET, 9000, 1, false, null),
    YELLOW_DIAMOND_BRACELET("yellow_diamond_bracelet", UndeadShopCategory.BRACELET, 8500, 1, false, null),
    EMERALD_BRACELET("emerald_bracelet", UndeadShopCategory.BRACELET, 8500, 1, false, null),
    BLUE_ZIRCON_BRACELET("blue_zircon_bracelet", UndeadShopCategory.BRACELET, 8500, 1, false, null),
    TOURMALINE_BRACELET("tourmaline_bracelet", UndeadShopCategory.BRACELET, 9000, 1, false, null),
    PURPLE_LITHIUM_BRACELET("purple_lithium_bracelet", UndeadShopCategory.BRACELET, 10000, 1, false, null),
    OBSIDIAN_BRACELET("obsidian_bracelet", UndeadShopCategory.BRACELET, 9000, 1, false, null),

    STRENGTH("strength", UndeadShopCategory.TALENT, 1500, 14, true, null),
    AGILITY("agility", UndeadShopCategory.TALENT, 1500, 14, true, null),
    INTELLIGENCE("intelligence", UndeadShopCategory.TALENT, 1500, 14, true, null),
    WILL("will", UndeadShopCategory.TALENT, 1500, 14, true, null),
    VITALITY("vitality", UndeadShopCategory.TALENT, 1500, 14, true, null),
    CRAFT("craft", UndeadShopCategory.TALENT, 1500, 12, true, null),

    WARM_CAMPFIRE("warm_campfire", UndeadShopCategory.SUPPORT, 3000, Integer.MAX_VALUE, false, null),
    ATTACK_FLAG("attack_flag", UndeadShopCategory.SUPPORT, 3500, Integer.MAX_VALUE, false, null),
    STAIRS("stairs", UndeadShopCategory.SUPPORT, 2000, Integer.MAX_VALUE, false, null),
    REST_TOMBSTONE("rest_tombstone", UndeadShopCategory.SUPPORT, 8000, Integer.MAX_VALUE, false, null),

    KNIGHT_INDESTRUCTIBLE("knight_indestructible", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.KNIGHT),
    KNIGHT_FANATIC_CHARGE("knight_fanatic_charge", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.KNIGHT),
    WARRIOR_INNER_POTENTIAL("warrior_inner_potential", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.WARRIOR),
    WARRIOR_FROZEN_BLADE("warrior_frozen_blade", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.WARRIOR),
    EXPLORER_THIRST_FOR_KNOWLEDGE("explorer_thirst_for_knowledge", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.EXPLORER),
    EXPLORER_SPIRIT("explorer_spirit", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.EXPLORER),
    ROGUE_SILENT("rogue_silent", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.ROGUE),
    ROGUE_HEAVY_BLOW("rogue_heavy_blow", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.ROGUE),
    SCHOLAR_ANCIENT_SCROLL("scholar_ancient_scroll", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.SCHOLAR),
    SCHOLAR_RETURNED("scholar_returned", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.SCHOLAR),
    HUNTER_DURABLE_FUEL("hunter_durable_fuel", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.HUNTER),
    HUNTER_HIGH_ENERGY_POWDER("hunter_high_energy_powder", UndeadShopCategory.TRAIT, 9000, 1, false,
            UndeadProfession.HUNTER);

    private final String id;
    private final UndeadShopCategory category;
    private final long price;
    private final int maxLevel;
    private final boolean refundable;
    private final UndeadProfession profession;

    UndeadShopEntry(
            String id,
            UndeadShopCategory category,
            long price,
            int maxLevel,
            boolean refundable,
            UndeadProfession profession
    ) {
        this.id = id;
        this.category = category;
        this.price = price;
        this.maxLevel = maxLevel;
        this.refundable = refundable;
        this.profession = profession;
    }

    public String id() {
        return id;
    }

    public UndeadShopCategory category() {
        return category;
    }

    public long price() {
        return price;
    }

    public long priceForLevel(int currentLevel) {
        int level = Math.max(0, currentLevel);
        if (this == TALENT_CAPACITY) {
            return saturatingAdd(price, level * 600L);
        }
        if (this == THOUSAND_TEMPERING) {
            long value = price;
            for (int i = 0; i < level; i++) {
                if (value > Long.MAX_VALUE / 2L) {
                    return Long.MAX_VALUE;
                }
                value *= 2L;
            }
            return value;
        }
        return price;
    }

    public int maxLevel() {
        return maxLevel;
    }

    public boolean refundable() {
        return refundable;
    }

    public Optional<UndeadProfession> profession() {
        return Optional.ofNullable(profession);
    }

    public boolean isBracelet() {
        return category == UndeadShopCategory.BRACELET;
    }

    public boolean isSupport() {
        return category == UndeadShopCategory.SUPPORT;
    }

    public boolean isUnlimited() {
        return maxLevel == Integer.MAX_VALUE;
    }

    public boolean requiresAllTalentAttributesMaxed() {
        return this == THOUSAND_TEMPERING;
    }

    public String nameKey() {
        return "screen.dealt_force_skills.undead_shop.entry." + id;
    }

    public String descriptionKey() {
        return nameKey() + ".description";
    }

    public static Optional<UndeadShopEntry> byId(String id) {
        return Arrays.stream(values()).filter(entry -> entry.id.equals(id)).findFirst();
    }

    private static long saturatingAdd(long left, long right) {
        if (right > 0L && left > Long.MAX_VALUE - right) {
            return Long.MAX_VALUE;
        }
        return left + right;
    }
}
