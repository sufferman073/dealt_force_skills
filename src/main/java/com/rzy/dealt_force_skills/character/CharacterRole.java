package com.rzy.dealt_force_skills.character;

import java.util.List;

public enum CharacterRole {
    ASSAULT("character.dealt_force_skills.role.assault"),
    SUPPORT("character.dealt_force_skills.role.support"),
    ENGINEER("character.dealt_force_skills.role.engineer"),
    RECON("character.dealt_force_skills.role.recon"),
    SPECIAL("character.dealt_force_skills.role.special");

    public static final List<CharacterRole> DISPLAY_ORDER = List.of(
            ASSAULT,
            SUPPORT,
            ENGINEER,
            RECON,
            SPECIAL
    );

    private final String translationKey;

    CharacterRole(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
