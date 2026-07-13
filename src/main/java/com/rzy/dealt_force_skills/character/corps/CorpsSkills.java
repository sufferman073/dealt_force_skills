package com.rzy.dealt_force_skills.character.corps;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class CorpsSkills {
    private CorpsSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        CorpsStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> CorpsStateManager.useActive1(player);
            case ACTIVE_2 -> CorpsStateManager.useActive2(player);
            case CORE -> {
                if (alternate) {
                    CorpsStateManager.cancelDuel(player);
                }
                yield true;
            }
            case PASSIVE -> false;
        };
    }

    public static boolean handleToolAction(ServerPlayer player, CorpsToolAction action, int targetEntityId) {
        if (!CorpsStateManager.isCorps(player)) {
            return false;
        }
        CorpsStateManager.initializeIfNeeded(player);
        return switch (action) {
            case START_DUEL -> CorpsStateManager.startDuel(player, targetEntityId);
            case CANCEL_DUEL -> {
                CorpsStateManager.cancelDuel(player);
                yield true;
            }
        };
    }
}
