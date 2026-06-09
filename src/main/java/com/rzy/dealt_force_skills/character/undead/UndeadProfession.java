package com.rzy.dealt_force_skills.character.undead;

public enum UndeadProfession {
    KNIGHT("knight"),
    WARRIOR("warrior"),
    EXPLORER("explorer"),
    ROGUE("rogue"),
    SCHOLAR("scholar"),
    HUNTER("hunter");

    private final String id;

    UndeadProfession(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public String translationKey() {
        return "character.dealt_force_skills.undead.profession." + id;
    }

    public static UndeadProfession byOrdinal(int ordinal) {
        UndeadProfession[] values = values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : KNIGHT;
    }
}
