package com.rzy.dealt_force_skills.character;

import com.rzy.dealt_force_skills.config.DealtForceConfig;

public record SkillDefinition(
        SkillSlot slot,
        String translationKey,
        String descriptionTranslationKey,
        int cooldownTicks,
        boolean implemented
) {
    public SkillDefinition {
        String key = translationKey
                .replace("character.dealt_force_skills.", "characters.")
                .replace(".skill.", ".skills.");
        cooldownTicks = DealtForceConfig.intValue(key + ".cooldown_ticks", cooldownTicks);
    }
}
