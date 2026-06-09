package com.rzy.dealt_force_skills.character.lexninjia;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public enum LexNinjiaArt {
    ONE_WORD_CUT("one_word_cut", LexNinjiaSchool.BLADE, 0, 0, 0, true, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE)),
    HANDSHAKE("handshake", LexNinjiaSchool.HAND, 0, 0, 0, true, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND)),
    SOLDIER_PILL("soldier_pill", LexNinjiaSchool.COOK, 0, 0, 0, true, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),

    FLASH_CUT_HAND("flash_cut_hand", LexNinjiaSchool.BLADE, 40, 1, 1800, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.LEFT_CLICK)),
    ONE_BLADE_TAUNT("one_blade_taunt", LexNinjiaSchool.BLADE, 25, 1, 1200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE)),
    BURNING_BLADE("burning_blade", LexNinjiaSchool.BLADE, 40, 2, 2600, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.LEFT_CLICK)),
    ARASHI_CUT("arashi_cut", LexNinjiaSchool.BLADE, 150, 3, 7200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    DEATH_FLAME_SMOKE("death_flame_smoke", LexNinjiaSchool.BLADE, 60, 2, 3600, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY)),
    SHADOW_BLADE("shadow_blade", LexNinjiaSchool.BLADE, 50, 2, 3000, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY)),
    SHADOW_SMOKE("shadow_smoke", LexNinjiaSchool.BLADE, 50, 2, 3400, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY)),
    CLIFF_FALL_BLADE("cliff_fall_blade", LexNinjiaSchool.BLADE, 15, 1, 1000, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.JUMP, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.LEFT_CLICK)),
    NO_NAME_BLADE("no_name_blade", LexNinjiaSchool.BLADE, 35, 2, 3200, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.LEFT_CLICK)),
    SHARPEN("sharpen", LexNinjiaSchool.BLADE, 75, 3, 4600, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY)),
    SHADOW_CLONE_CROSS("shadow_clone_cross", LexNinjiaSchool.BLADE, 140, 4, 8500, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    TEN_METER_SWORD("ten_meter_sword", LexNinjiaSchool.BLADE, 100, 3, 6200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY)),
    IRON_SWORD_RAIN("iron_sword_rain", LexNinjiaSchool.BLADE, 190, 5, 11000, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY)),

    FD_HAND("fd_hand", LexNinjiaSchool.HAND, 35, 1, 1600, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    FIRE_FIST("fire_fist", LexNinjiaSchool.HAND, 20, 1, 1200, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.LEFT_CLICK)),
    LUOHAN_HAND("luohan_hand", LexNinjiaSchool.HAND, 30, 1, 1800, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.LEFT_CLICK)),
    REFLECT_HAND("reflect_hand", LexNinjiaSchool.HAND, 70, 2, 4200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    STOP_HAND("stop_hand", LexNinjiaSchool.HAND, 100, 3, 5600, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.LEFT_CLICK)),
    PEA_SHOOTER("pea_shooter", LexNinjiaSchool.HAND, 15, 2, 1700, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND)),
    BIG_PORTION("big_portion", LexNinjiaSchool.HAND, 30, 1, 2200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    GOOD_SLEEP("good_sleep", LexNinjiaSchool.HAND, 0, 1, 2600, false, false, 0, LexNinjiaReleaseTrigger.RIGHT_RELEASE,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.RIGHT_RELEASE)),
    RETURN_HAND("return_hand", LexNinjiaSchool.HAND, 100, 3, 6200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND)),
    DOUBLE_LUOHAN("double_luohan", LexNinjiaSchool.HAND, 45, 3, 3200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    ION_HAND("ion_hand", LexNinjiaSchool.HAND, 50, 2, 3600, false, false, 0, LexNinjiaReleaseTrigger.RIGHT_RELEASE,
            combo(LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.HAND, LexNinjiaComboInput.RIGHT_RELEASE)),
    SPIN_ION_HAND("spin_ion_hand", LexNinjiaSchool.HAND, 20, 4, 6000, false, false, 0, LexNinjiaReleaseTrigger.RIGHT_RELEASE,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.RIGHT_RELEASE)),
    WHITE_CRANE("white_crane", LexNinjiaSchool.HAND, 40, 2, 2600, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND)),
    SHIELD_GUARD("shield_guard", LexNinjiaSchool.HAND, 60, 2, 3800, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY)),
    SAND_WALL("sand_wall", LexNinjiaSchool.HAND, 100, 3, 5200, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE)),
    NO_ONE_RETALIATES("no_one_retaliates", LexNinjiaSchool.HAND, 150, 5, 9000, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY)),
    ONE_DEATH_HAND("one_death_hand", LexNinjiaSchool.HAND, 200, 5, 11500, false, false, 0, LexNinjiaReleaseTrigger.LEFT_CLICK,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.LEFT_CLICK)),
    SNAKE_POISON_HAND("snake_poison_hand", LexNinjiaSchool.HAND, 150, 5, 9800, false, false, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.JUMP, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND)),
    DEATH_GOD_HAND("death_god_hand", LexNinjiaSchool.HAND, 250, 5, 12500, false, false, 250, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE)),
    ALL_HANDS("all_hands", LexNinjiaSchool.HAND, 1500, 12, 50000, false, false, 1500, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HAND)),

    NANO_SNICKERS("nano_snickers", LexNinjiaSchool.COOK, 0, 0, 900, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    HAMBURGER("hamburger", LexNinjiaSchool.COOK, 0, 0, 1100, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    MILK_FRUIT_SHAKE("milk_fruit_shake", LexNinjiaSchool.COOK, 0, 0, 1200, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    SHRIMP_HAND("shrimp_hand", LexNinjiaSchool.COOK, 0, 0, 1300, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    ROAST_MEAT_RICE("roast_meat_rice", LexNinjiaSchool.COOK, 0, 0, 1700, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    MC_NUGGETS("mc_nuggets", LexNinjiaSchool.COOK, 0, 0, 1200, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    LOTUS_BOX_FOOD("lotus_box_food", LexNinjiaSchool.COOK, 0, 0, 1500, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    MILK_BEER("milk_beer", LexNinjiaSchool.COOK, 0, 0, 1600, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    COLD_COPPER("cold_copper", LexNinjiaSchool.COOK, 0, 0, 800, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),
    HOT_DRINK("hot_drink", LexNinjiaSchool.COOK, 0, 0, 2200, false, false, 0, LexNinjiaReleaseTrigger.COOK, List.of()),

    HAM_FRIEND("ham_friend", LexNinjiaSchool.HAM, 0, 5, 25000, false, true, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY)),
    HAM_BERSERK("ham_berserk", LexNinjiaSchool.HAM, 0, 7, 32000, false, true, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HARMONY)),
    HAM_KILL_ALL("ham_kill_all", LexNinjiaSchool.HAM, 0, 12, 45000, false, true, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.BLADE)),
    HAM_BEAST("ham_beast", LexNinjiaSchool.HAM, 0, 10, 38000, false, true, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE, LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.HAND, LexNinjiaComboInput.BLADE)),
    HAM_SHADOW_KICK("ham_shadow_kick", LexNinjiaSchool.HAM, 0, 9, 36000, false, true, 0, LexNinjiaReleaseTrigger.MANUAL,
            combo(LexNinjiaComboInput.HARMONY, LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.JUMP, LexNinjiaComboInput.SNEAK, LexNinjiaComboInput.JUMP, LexNinjiaComboInput.HARMONY));

    private final String id;
    private final LexNinjiaSchool school;
    private final int leicraCost;
    private final int mindCost;
    private final long price;
    private final boolean defaultKnown;
    private final boolean hamForbidden;
    private final int requiredMaxLeicra;
    private final LexNinjiaReleaseTrigger releaseTrigger;
    private final List<LexNinjiaComboInput> combo;

    LexNinjiaArt(String id, LexNinjiaSchool school, int leicraCost, int mindCost, long price,
                 boolean defaultKnown, boolean hamForbidden, int requiredMaxLeicra,
                 LexNinjiaReleaseTrigger releaseTrigger, List<LexNinjiaComboInput> combo) {
        this.id = id;
        this.school = school;
        this.leicraCost = leicraCost;
        this.mindCost = mindCost;
        this.price = price;
        this.defaultKnown = defaultKnown;
        this.hamForbidden = hamForbidden;
        this.requiredMaxLeicra = requiredMaxLeicra;
        this.releaseTrigger = releaseTrigger;
        this.combo = List.copyOf(combo);
    }

    public String id() {
        return id;
    }

    public LexNinjiaSchool school() {
        return school;
    }

    public int leicraCost() {
        return leicraCost;
    }

    public int mindCost() {
        return mindCost;
    }

    public long price() {
        return price;
    }

    public boolean defaultKnown() {
        return defaultKnown;
    }

    public boolean hamForbidden() {
        return hamForbidden;
    }

    public int requiredMaxLeicra() {
        return requiredMaxLeicra;
    }

    public LexNinjiaReleaseTrigger releaseTrigger() {
        return releaseTrigger;
    }

    public List<LexNinjiaComboInput> combo() {
        return combo;
    }

    public String nameKey() {
        return "lex_ninjia.art.dealt_force_skills." + id + ".name";
    }

    public String descriptionKey() {
        return "lex_ninjia.art.dealt_force_skills." + id + ".desc";
    }

    public String comboKey() {
        return "lex_ninjia.art.dealt_force_skills." + id + ".combo";
    }

    public String soundId() {
        return "lex_ninjia_" + id;
    }

    public boolean cookRecipe() {
        return releaseTrigger == LexNinjiaReleaseTrigger.COOK;
    }

    public static Optional<LexNinjiaArt> byId(String id) {
        return Arrays.stream(values()).filter(art -> art.id.equals(id)).findFirst();
    }

    private static List<LexNinjiaComboInput> combo(LexNinjiaComboInput... inputs) {
        return List.of(inputs);
    }
}
