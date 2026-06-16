package com.rzy.dealt_force_skills.skill;

public enum SkillModelVisual {
    MANBA_FLASHLIGHT_TOGGLE(3, 2),
    MANBA_ELBOW(5, 2),
    NIKAIDOU_HOT_IRON(6, 3),
    NIKAIDOU_RITUAL_SWORD(5, 2),
    CATDAD_GUARD(4, 0),
    CATDAD_CLAW(5, 3),
    UNDEAD_KNIGHT_CHARGE(4, 2),
    UNDEAD_KNIGHT_BARRIER(6, 3),
    UNDEAD_KNIGHT_WALL(14, 3),
    UNDEAD_WARRIOR_AXE(5, 3),
    UNDEAD_EXPLORER_LANTERN(5, 4),
    UNDEAD_EXPLORER_LANTERN_HOLD(8, 0),
    UNDEAD_ROGUE_BATON(5, 4),
    UNDEAD_SCHOLAR_STITCH(6, 4),
    UNDEAD_SCHOLAR_FIBERS(14, 3),
    UNDEAD_SCHOLAR_RITUAL(20, 4),
    LEX_HAND_REACH(4, 2),
    LEX_HAM_MANIFEST(6, 0),
    LEX_HAM_SURGE(5, 3),
    LEX_HAM_PRESENCE(14, 0),
    NIKAIDOU_HOT_IRON_OVERHEAD(6, 3),
    NIKAIDOU_RITUAL_SWORD_RIGHT_TO_LEFT(5, 2),
    NIKAIDOU_RITUAL_SWORD_DIAGONAL(6, 3);

    private final int durationTicks;
    private final int impactTick;

    SkillModelVisual(int durationTicks, int impactTick) {
        this.durationTicks = durationTicks;
        this.impactTick = impactTick;
    }

    public int durationTicks() {
        return durationTicks;
    }

    public int impactTick() {
        return impactTick;
    }

    public static SkillModelVisual byId(int id) {
        SkillModelVisual[] values = values();
        return id >= 0 && id < values.length ? values[id] : null;
    }
}
