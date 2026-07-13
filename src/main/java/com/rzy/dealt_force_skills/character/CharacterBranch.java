package com.rzy.dealt_force_skills.character;

import java.util.List;

public record CharacterBranch(
        String id,
        String name,
        boolean nameTranslationKey,
        List<CharacterDefinition> characters
) {
    public CharacterBranch {
        characters = List.copyOf(characters);
    }
}
