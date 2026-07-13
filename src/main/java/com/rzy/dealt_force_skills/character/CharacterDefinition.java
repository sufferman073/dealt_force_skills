package com.rzy.dealt_force_skills.character;

import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;

public record CharacterDefinition(
        String id,
        String nameTranslationKey,
        boolean nameIsTranslationKey,
        CharacterRole role,
        String sourcePath,
        boolean selectable,
        List<SkillDefinition> skills
) {
    public CharacterDefinition(String id, String nameTranslationKey, CharacterRole role, String sourcePath,
                               List<SkillDefinition> skills) {
        this(id, nameTranslationKey, true, role, sourcePath, true, skills);
    }

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

    public Component displayName() {
        return nameIsTranslationKey ? Component.translatable(nameTranslationKey) : Component.literal(nameTranslationKey);
    }
}
