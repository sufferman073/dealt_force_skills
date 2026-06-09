package com.rzy.dealt_force_skills.character;

public enum SkillSlot {
    PASSIVE,
    ACTIVE_1,
    ACTIVE_2,
    CORE;

    public boolean canBeTriggeredByKey() {
        return this != PASSIVE;
    }
}
