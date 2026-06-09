package com.rzy.dealt_force_skills.character.manba;

public enum ManbaTalent {
    GOOD_REWARD("善有善报"),
    SELF_TAUGHT("自学成才"),
    DIVERSION("声东击西"),
    STEEL_BODY("钢筋铁骨"),
    WATCHER("守夜人"),
    OPPORTUNITY_WINDOW("机遇之窗"),
    NANNY("保姆"),
    DURABLE("经久耐用"),
    BRAVE_FORWARD("勇往直前"),
    INDESTRUCTIBLE("坚不可摧"),
    UNYIELDING("百折不挠"),
    LIGHT_WARRIOR("光明勇士");

    private final String displayName;

    ManbaTalent(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
