package com.rzy.dealt_force_skills.shop;

public enum UndeadShopCategory {
    SERVICE,
    BRACELET,
    TALENT,
    SUPPORT,
    TRAIT;

    public String translationKey() {
        return "screen.dealt_force_skills.undead_shop.category." + name().toLowerCase();
    }
}
