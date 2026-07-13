package com.rzy.dealt_force_skills.character;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import net.minecraft.network.chat.Component;

public record SkillDefinition(
        SkillSlot slot,
        String translationKey,
        boolean translationIsKey,
        String descriptionTranslationKey,
        boolean descriptionIsKey,
        int cooldownTicks,
        boolean implemented
) {
    public SkillDefinition(SkillSlot slot, String translationKey, String descriptionTranslationKey,
                           int cooldownTicks, boolean implemented) {
        this(slot, translationKey, true, descriptionTranslationKey, true, cooldownTicks, implemented);
    }

    public SkillDefinition {
        if (translationIsKey && translationKey != null && translationKey.startsWith("character.dealt_force_skills.")) {
            String key = translationKey
                    .replace("character.dealt_force_skills.", "characters.")
                    .replace(".skill.", ".skills.");
            cooldownTicks = DealtForceConfig.intValue(key + ".cooldown_ticks", cooldownTicks);
        }
    }

    public Component displayName() {
        return translationIsKey ? Component.translatable(translationKey) : Component.literal(translationKey);
    }

    public Component displayDescription() {
        return descriptionIsKey
                ? Component.translatable(descriptionTranslationKey)
                : Component.literal(descriptionTranslationKey);
    }
}
