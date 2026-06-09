package com.rzy.dealt_force_skills.character;

import java.util.List;
import java.util.Optional;

public record CharacterDefinition(
        String id,
        String nameTranslationKey,
        CharacterRole role,
        String sourcePath,
        List<SkillDefinition> skills
) {
    public CharacterDefinition {
        skills = List.copyOf(skills);
    }

    public Optional<SkillDefinition> skill(SkillSlot slot) {
        return skills.stream()
                .filter(skill -> skill.slot() == slot)
                .findFirst();
    }

    public String roleTranslationKey() {
        return role.translationKey();
    }
}
