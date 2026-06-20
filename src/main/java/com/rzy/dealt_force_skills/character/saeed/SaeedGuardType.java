package com.rzy.dealt_force_skills.character.saeed;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.Arrays;
import java.util.Optional;

public enum SaeedGuardType {
    THUNDER("thunder", 1, 100, 2, 16, 80.0D, 10.0D, 8.0D, 0.25D, 6.0D, 18.0D,
            Items.IRON_SWORD, Items.SHIELD),
    IRON_RAIN("iron_rain", 2, 200, 6, 16, 100.0D, 25.0D, 10.0D, 0.16D, 5.0D, 18.0D,
            Items.GUNPOWDER, Items.AIR),
    FIREEYE("fireeye", 3, 250, 5, 16, 50.0D, 5.0D, 3.0D, 0.25D, 7.5D, 7.0D,
            Items.BLAZE_ROD, Items.AIR),
    SHARP_EAGLE("sharp_eagle", 3, 400, 4, 16, 18.0D, 0.0D, 0.0D, 0.24D, 4.0D, 34.0D,
            Items.SPYGLASS, Items.AIR),
    HAKIM("hakim", 4, 500, 6, 1, 60.0D, 12.0D, 6.0D, 0.31D, 16.0D, 26.0D,
            Items.FIREWORK_ROCKET, Items.AIR),
    KARIM("karim", 4, 500, 6, 1, 70.0D, 16.0D, 8.0D, 0.22D, 20.0D, 7.0D,
            Items.BLAZE_ROD, Items.SHIELD);

    private final String id;
    private final int requiredPrestige;
    private final int cost;
    private final int population;
    private final int maxActive;
    private final double maxHealth;
    private final double armor;
    private final double toughness;
    private final double speed;
    private final double attackDamage;
    private final double skillRange;
    private final Item mainHand;
    private final Item offHand;

    SaeedGuardType(
            String id,
            int requiredPrestige,
            int cost,
            int population,
            int maxActive,
            double maxHealth,
            double armor,
            double toughness,
            double speed,
            double attackDamage,
            double skillRange,
            Item mainHand,
            Item offHand
    ) {
        this.id = id;
        String key = "characters.saeed.summons." + id;
        this.requiredPrestige = DealtForceConfig.intValue(key + ".required_prestige", requiredPrestige);
        this.cost = DealtForceConfig.intValue(key + ".cost", cost);
        this.population = DealtForceConfig.intValue(key + ".population", population);
        this.maxActive = DealtForceConfig.intValue(key + ".max_active", maxActive);
        this.maxHealth = DealtForceConfig.doubleValue(key + ".max_health", maxHealth);
        this.armor = DealtForceConfig.doubleValue(key + ".armor", armor);
        this.toughness = DealtForceConfig.doubleValue(key + ".toughness", toughness);
        this.speed = DealtForceConfig.doubleValue(key + ".speed", speed);
        this.attackDamage = DealtForceConfig.doubleValue(key + ".attack_damage", attackDamage);
        this.skillRange = DealtForceConfig.doubleValue(key + ".skill_range", skillRange);
        this.mainHand = mainHand;
        this.offHand = offHand;
    }

    public String id() {
        return id;
    }

    public String nameKey() {
        return "screen.dealt_force_skills.saeed_recruit.guard." + id;
    }

    public String descriptionKey() {
        return nameKey() + ".description";
    }

    public int requiredPrestige() {
        return requiredPrestige;
    }

    public int cost() {
        return cost;
    }

    public int population() {
        return population;
    }

    public int maxActive() {
        return maxActive;
    }

    public double maxHealth() {
        return maxHealth;
    }

    public double armor() {
        return armor;
    }

    public double toughness() {
        return toughness;
    }

    public double speed() {
        return speed;
    }

    public double attackDamage() {
        return attackDamage;
    }

    public double skillRange() {
        return skillRange;
    }

    public double targetRange() {
        double defaultValue = switch (this) {
            case FIREEYE -> 35.0D;
            case HAKIM -> 80.0D;
            case SHARP_EAGLE -> 150.0D;
            default -> 70.0D;
        };
        return DealtForceConfig.doubleValue("characters.saeed.summons." + id + ".target_range", defaultValue);
    }

    public double attackRange() {
        return targetRange();
    }

    public boolean usesFireStream() {
        return this == FIREEYE || this == KARIM;
    }

    public boolean usesGunfire() {
        return this == THUNDER || this == IRON_RAIN || this == SHARP_EAGLE;
    }

    public boolean usesRocket() {
        return this == HAKIM;
    }

    public boolean usesRangedBurst() {
        return usesGunfire() || usesRocket();
    }

    public boolean isLeaderGuard() {
        return this == HAKIM || this == KARIM;
    }

    public ItemStack mainHand() {
        return mainHand == Items.AIR ? ItemStack.EMPTY : new ItemStack(mainHand);
    }

    public ItemStack offHand() {
        return offHand == Items.AIR ? ItemStack.EMPTY : new ItemStack(offHand);
    }

    public static Optional<SaeedGuardType> byId(String id) {
        return Arrays.stream(values())
                .filter(type -> type.id.equals(id))
                .findFirst();
    }

    public static SaeedGuardType byOrdinal(int ordinal) {
        SaeedGuardType[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : THUNDER;
    }
}
