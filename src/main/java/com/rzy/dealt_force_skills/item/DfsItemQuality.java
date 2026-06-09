package com.rzy.dealt_force_skills.item;

import net.minecraft.world.item.ItemStack;

public enum DfsItemQuality {
    WHITE("white", 0, 0xFFFFFF),
    GREEN("green", 1, 0x55FF55),
    BLUE("blue", 2, 0x55AAFF),
    PURPLE("purple", 3, 0xAA55FF),
    GOLD("gold", 4, 0xFFD34D),
    RED("red", 5, 0xFF5555);

    private final String key;
    private final int tier;
    private final int color;

    DfsItemQuality(String key, int tier, int color) {
        this.key = key;
        this.tier = tier;
        this.color = color;
    }

    public String key() {
        return key;
    }

    public int tier() {
        return tier;
    }

    public int color() {
        return color;
    }

    public static DfsItemQuality of(ItemStack stack) {
        if (stack.isEmpty()) {
            return WHITE;
        }
        if (stack.getItem() instanceof QualityTooltipItem qualityItem) {
            return qualityItem.quality();
        }
        if (stack.getItem() instanceof DfsEquipmentItem equipmentItem) {
            return equipmentItem.profile().quality();
        }
        return WHITE;
    }
}
