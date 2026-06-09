package com.rzy.dealt_force_skills.character.lexninjia;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class LexNinjiaSkills {
    private LexNinjiaSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        return switch (slot) {
            case ACTIVE_1 -> LexNinjiaStateManager.useFoundationSkill(player, LexNinjiaComboInput.HAND, 3);
            case ACTIVE_2 -> LexNinjiaStateManager.useFoundationSkill(player, LexNinjiaComboInput.BLADE, 3);
            case CORE -> LexNinjiaStateManager.useFoundationSkill(player, LexNinjiaComboInput.HARMONY, 5);
            default -> false;
        };
    }
}
