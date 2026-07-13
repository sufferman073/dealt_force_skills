package com.rzy.dealt_force_skills.character.gambler;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class GamblerSkills {
    private GamblerSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        GamblerStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> GamblerStateManager.useFinalBet(player, alternate);
            case ACTIVE_2 -> GamblerStateManager.useHakkoIchiu(player);
            case CORE -> GamblerStateManager.toggleCore(player);
            default -> false;
        };
    }
}
