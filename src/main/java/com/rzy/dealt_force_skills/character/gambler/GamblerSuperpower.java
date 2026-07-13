package com.rzy.dealt_force_skills.character.gambler;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public enum GamblerSuperpower {
    RIDE_THE_WIND("ride_the_wind", GamblerRarity.RED, GamblerTargetMode.PLAYER),
    EARTHSHAKING_SLAM("earthshaking_slam", GamblerRarity.RED, GamblerTargetMode.LIVING),
    TRIPLE_THREAT("triple_threat", GamblerRarity.RED, GamblerTargetMode.NONE),
    SHRINK_RAY("shrink_ray", GamblerRarity.RED, GamblerTargetMode.LIVING),
    VITALITY_BURST("vitality_burst", GamblerRarity.GOLD, GamblerTargetMode.LIVING),
    FROZEN_TUNDRA("frozen_tundra", GamblerRarity.RED, GamblerTargetMode.NONE),
    INSPIRATION("inspiration", GamblerRarity.RED, GamblerTargetMode.NONE),
    WITCH_FAMILIAR("witch_familiar", GamblerRarity.RED, GamblerTargetMode.NONE),
    MISSILE_BARRAGE("missile_barrage", GamblerRarity.RED, GamblerTargetMode.LIVING),
    OCTO_THROWER("octo_thrower", GamblerRarity.GOLD, GamblerTargetMode.NONE),
    SCARY_SHAPER_1000("scary_shaper_1000", GamblerRarity.RED, GamblerTargetMode.NONE),
    ICE_MOON("ice_moon", GamblerRarity.RED, GamblerTargetMode.NONE),
    BISECT("bisect", GamblerRarity.GOLD, GamblerTargetMode.LIVING),
    TELEPATHY("telepathy", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    STINK_CLOUD("stink_cloud", GamblerRarity.GOLD, GamblerTargetMode.NONE),
    HEROIC_HEALING("heroic_healing", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    POSSESSION("possession", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    ZOMBIE_MORALE("zombie_morale", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    TOMBSTONE_CODE("tombstone_code", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    BRUTE_FORCE("brute_force", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    STONE_WALL("stone_wall", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    DANCE_OFF("dance_off", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    EVAPORATE("evaporate", GamblerRarity.GOLD, GamblerTargetMode.LIVING),
    LIGHTNING_ARROW("lightning_arrow", GamblerRarity.PURPLE, GamblerTargetMode.LIVING),
    DOLPHIN_HURRICANE("dolphin_hurricane", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    ACID_RAIN("acid_rain", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    SUMMONING("summoning", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    PRECISION_STRIKE("precision_strike", GamblerRarity.GOLD, GamblerTargetMode.LIVING),
    SOLAR_BURN("solar_burn", GamblerRarity.RED, GamblerTargetMode.LIVING),
    UNBREAKABLE("unbreakable", GamblerRarity.RED, GamblerTargetMode.NONE),
    DEVOUR("devour", GamblerRarity.RED, GamblerTargetMode.LIVING),
    THROW_POTATO("throw_potato", GamblerRarity.RED, GamblerTargetMode.NONE),
    ORANGE_PEEL_SHIELD("orange_peel_shield", GamblerRarity.RED, GamblerTargetMode.NONE),
    POWER_PUNCH("power_punch", GamblerRarity.GOLD, GamblerTargetMode.NONE),
    INFLATING_MUSHROOM("inflating_mushroom", GamblerRarity.GOLD, GamblerTargetMode.LIVING),
    SHEEPIFY("sheepify", GamblerRarity.RED, GamblerTargetMode.NON_PLAYER),
    BLAZING_BARK("blazing_bark", GamblerRarity.RED, GamblerTargetMode.PLAYER),
    GENE_AMPLIFICATION("gene_amplification", GamblerRarity.RED, GamblerTargetMode.PLAYER),
    UPROOT_FLAG("uproot_flag", GamblerRarity.PURPLE, GamblerTargetMode.LIVING),
    LIEUTENANT_CARROT("lieutenant_carrot", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    LIGHTSPEED_SEED("lightspeed_seed", GamblerRarity.GOLD, GamblerTargetMode.NONE),
    METEOR_IMPACT("meteor_impact", GamblerRarity.PURPLE, GamblerTargetMode.LIVING),
    GIGANTIFY("gigantify", GamblerRarity.PURPLE, GamblerTargetMode.PLAYER),
    RADIANT("radiant", GamblerRarity.GOLD, GamblerTargetMode.PLAYER),
    POLYMORPH("polymorph", GamblerRarity.GOLD, GamblerTargetMode.NON_PLAYER),
    FREEZE("freeze", GamblerRarity.PURPLE, GamblerTargetMode.LIVING),
    WEED_ATTACK("weed_attack", GamblerRarity.PURPLE, GamblerTargetMode.LIVING),
    SPORE_OVERLOAD("spore_overload", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    RAINSTORM("rainstorm", GamblerRarity.GOLD, GamblerTargetMode.NONE),
    HOLOGRAM_FLOWER("hologram_flower", GamblerRarity.GOLD, GamblerTargetMode.NONE),
    ROOTING_WALL("rooting_wall", GamblerRarity.GOLD, GamblerTargetMode.LIVING),
    BUBBLES("bubbles", GamblerRarity.GOLD, GamblerTargetMode.PLAYER),
    SCORCHED_EARTH("scorched_earth", GamblerRarity.PURPLE, GamblerTargetMode.NONE),
    FOUNTAIN("fountain", GamblerRarity.PURPLE, GamblerTargetMode.NONE);

    private static final GamblerSuperpower[] VALUES = values();

    private final String id;
    private final GamblerRarity rarity;
    private final GamblerTargetMode targetMode;

    GamblerSuperpower(String id, GamblerRarity rarity, GamblerTargetMode targetMode) {
        this.id = id;
        this.rarity = rarity;
        this.targetMode = targetMode;
    }

    public String id() {
        return id;
    }

    public GamblerRarity rarity() {
        return rarity;
    }

    public GamblerTargetMode targetMode() {
        return targetMode;
    }

    public String nameKey() {
        return "character.dealt_force_skills.gambler.power." + id;
    }

    public String descriptionKey() {
        return nameKey() + ".desc";
    }

    public static GamblerSuperpower byOrdinal(int ordinal) {
        return ordinal >= 0 && ordinal < VALUES.length ? VALUES[ordinal] : null;
    }

    public static GamblerSuperpower randomAny(Random random) {
        return VALUES[random.nextInt(VALUES.length)];
    }

    public static GamblerSuperpower randomByRarity(Random random, GamblerRarity rarity) {
        List<GamblerSuperpower> matches = new ArrayList<>();
        for (GamblerSuperpower power : VALUES) {
            if (power.rarity == rarity) {
                matches.add(power);
            }
        }
        if (matches.isEmpty()) {
            return randomAny(random);
        }
        return matches.get(random.nextInt(matches.size()));
    }

    public static GamblerSuperpower randomAtLeastGold(Random random) {
        List<GamblerSuperpower> matches = new ArrayList<>();
        for (GamblerSuperpower power : VALUES) {
            if (power.rarity == GamblerRarity.RED || power.rarity == GamblerRarity.GOLD) {
                matches.add(power);
            }
        }
        return matches.get(random.nextInt(matches.size()));
    }

    public String debugId() {
        return name().toLowerCase(Locale.ROOT);
    }
}
