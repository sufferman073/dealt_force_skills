package com.rzy.dealt_force_skills.character.manba;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class ManbaSkills {
    private ManbaSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        ManbaStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> {
                ManbaStateManager.useElbow(player);
                yield true;
            }
            case ACTIVE_2 -> {
                ManbaStateManager.setFlashlightActive(player, true);
                yield true;
            }
            case CORE -> {
                if (alternate) {
                    ManbaStateManager.cancelDuel(player);
                }
                yield true;
            }
            case PASSIVE -> false;
        };
    }

    public static boolean handleToolAction(ServerPlayer player, ManbaToolAction action, int targetEntityId) {
        if (!ManbaStateManager.isManba(player)) {
            return false;
        }
        ManbaStateManager.initializeIfNeeded(player);
        return switch (action) {
            case START_FLASHLIGHT -> {
                ManbaStateManager.setFlashlightActive(player, true);
                yield true;
            }
            case STOP_FLASHLIGHT -> {
                ManbaStateManager.setFlashlightActive(player, false);
                yield true;
            }
            case START_DUEL -> ManbaStateManager.startDuel(player, targetEntityId);
            case CANCEL_DUEL -> {
                ManbaStateManager.cancelDuel(player);
                yield true;
            }
        };
    }
}
