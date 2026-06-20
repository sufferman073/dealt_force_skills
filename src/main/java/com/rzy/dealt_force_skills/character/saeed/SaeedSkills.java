package com.rzy.dealt_force_skills.character.saeed;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class SaeedSkills {
    private SaeedSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        SaeedStateManager.initializeIfNeeded(player);
        if (SaeedStateManager.isCoreBoostActive(player)) {
            return switch (slot) {
                case ACTIVE_1 -> SaeedStateManager.useRoll(player);
                case ACTIVE_2 -> SaeedStateManager.useFireArrow(player, alternate);
                case CORE -> alternate ? SaeedStateManager.clearCommandPoint(player) : SaeedStateManager.markCommandPoint(player);
                case PASSIVE -> false;
            };
        }
        return switch (slot) {
            case ACTIVE_1 -> SaeedStateManager.useRoll(player);
            case ACTIVE_2 -> SaeedStateManager.useFireArrow(player, alternate);
            case CORE -> SaeedStateManager.useCore(player);
            case PASSIVE -> false;
        };
    }
}
