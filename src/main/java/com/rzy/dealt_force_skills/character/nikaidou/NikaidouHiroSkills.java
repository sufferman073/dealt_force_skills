package com.rzy.dealt_force_skills.character.nikaidou;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class NikaidouHiroSkills {
    private NikaidouHiroSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        NikaidouHiroStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> NikaidouHiroStateManager.useCorrection(player);
            case ACTIVE_2 -> NikaidouHiroStateManager.toggleHotIron(player);
            case CORE -> NikaidouHiroStateManager.startCore(player);
            case PASSIVE -> false;
        };
    }

    public static boolean handleToolAction(ServerPlayer player, NikaidouHiroToolAction action) {
        return NikaidouHiroStateManager.handleToolAction(player, action);
    }
}
