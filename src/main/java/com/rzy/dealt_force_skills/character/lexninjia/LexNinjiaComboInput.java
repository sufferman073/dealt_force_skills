package com.rzy.dealt_force_skills.character.lexninjia;

public enum LexNinjiaComboInput {
    HAND("hud.dealt_force_skills.lex_ninjia.input.hand"),
    BLADE("hud.dealt_force_skills.lex_ninjia.input.blade"),
    HARMONY("hud.dealt_force_skills.lex_ninjia.input.harmony"),
    SNEAK("hud.dealt_force_skills.lex_ninjia.input.sneak"),
    JUMP("hud.dealt_force_skills.lex_ninjia.input.jump"),
    LEFT_CLICK("hud.dealt_force_skills.lex_ninjia.input.left_click"),
    RIGHT_RELEASE("hud.dealt_force_skills.lex_ninjia.input.right_release");

    private final String translationKey;

    LexNinjiaComboInput(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
