package com.rzy.dealt_force_skills.character.gambler;

import java.util.Random;

public enum GamblerDrawEffect {
    LAND_SEA_AIR("land_sea_air", 1, 15.0D, false),
    MILITARISM("militarism", 2, 15.0D, false),
    FIFTH_INFANTRY("fifth_infantry", 4, 15.0D, false),
    FORTIETH_CAVALRY("fortieth_cavalry", 3, 15.0D, true),
    WIDE_LAND("wide_land", 4, 5.0D, true),
    DESPERATE_PLAN("desperate_plan", 2, 10.0D, false),
    IMPERIAL_POWER("imperial_power", 0, 10.0D, false),
    VIRAL_SPREAD("viral_spread", 3, 3.0D, true),
    THINKING_ZOMBIE("thinking_zombie", 4, 3.0D, true),
    CROWDFUND_REVIVAL("crowdfund_revival", 3, 5.0D, true),
    LECTURE_ZOMBIE("lecture_zombie", 1, 2.0D, true),
    HAKKO_ICHIIU("hakko_ichiiu", 4, 2.0D, true);

    private static final GamblerDrawEffect[] VALUES = values();

    private final String id;
    private final int cost;
    private final double baseWeight;
    private final boolean good;

    GamblerDrawEffect(String id, int cost, double baseWeight, boolean good) {
        this.id = id;
        this.cost = cost;
        this.baseWeight = baseWeight;
        this.good = good;
    }

    public String id() {
        return id;
    }

    public int cost() {
        return cost;
    }

    public String nameKey() {
        return "character.dealt_force_skills.gambler.draw." + id;
    }

    public String descriptionKey() {
        return nameKey() + ".desc";
    }

    public boolean isHakko() {
        return this == HAKKO_ICHIIU;
    }

    public static GamblerDrawEffect roll(Random random, double luck, boolean forceHakko) {
        if (forceHakko) {
            return HAKKO_ICHIIU;
        }
        double total = 0.0D;
        double[] weights = new double[VALUES.length];
        for (int i = 0; i < VALUES.length; i++) {
            GamblerDrawEffect effect = VALUES[i];
            double weight = effect.baseWeight;
            double luckShift = effect.good ? 2.0D * luck : -2.0D * luck;
            weight = Math.max(0.0D, weight + luckShift);
            weights[i] = weight;
            total += weight;
        }
        if (total <= 0.0D) {
            return VALUES[random.nextInt(VALUES.length)];
        }
        double roll = random.nextDouble() * total;
        for (int i = 0; i < VALUES.length; i++) {
            roll -= weights[i];
            if (roll <= 0.0D) {
                return VALUES[i];
            }
        }
        return VALUES[VALUES.length - 1];
    }
}
