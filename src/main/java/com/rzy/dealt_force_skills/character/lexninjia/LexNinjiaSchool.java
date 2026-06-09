package com.rzy.dealt_force_skills.character.lexninjia;

public enum LexNinjiaSchool {
    BLADE("screen.dealt_force_skills.lex_ninjia_shop.school.blade"),
    HAND("screen.dealt_force_skills.lex_ninjia_shop.school.hand"),
    COOK("screen.dealt_force_skills.lex_ninjia_shop.school.cook"),
    HAM("screen.dealt_force_skills.lex_ninjia_shop.school.ham");

    private final String translationKey;

    LexNinjiaSchool(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
