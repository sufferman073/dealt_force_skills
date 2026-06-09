package com.rzy.dealt_force_skills.character;

public record SkillDefinition(
        SkillSlot slot,
        String translationKey,
        String descriptionTranslationKey,
        int cooldownTicks,
        boolean implemented
) {
}
