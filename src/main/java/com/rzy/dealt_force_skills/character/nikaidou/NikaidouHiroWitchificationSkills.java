package com.rzy.dealt_force_skills.character.nikaidou;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class NikaidouHiroWitchificationSkills {
    private NikaidouHiroWitchificationSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        NikaidouHiroWitchificationStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> NikaidouHiroWitchificationStateManager.placeRewindAnchor(player);
            case ACTIVE_2 -> NikaidouHiroWitchificationStateManager.eraseErrors(player);
            case CORE -> NikaidouHiroWitchificationStateManager.toggleCore(player);
            case PASSIVE -> false;
        };
    }

    public static boolean handleToolAction(ServerPlayer player, NikaidouHiroToolAction action) {
        return NikaidouHiroWitchificationStateManager.handleToolAction(player, action);
    }
}
