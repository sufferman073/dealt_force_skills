package com.rzy.dealt_force_skills.character.ghroth;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class GhrothSkills {
    private GhrothSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        GhrothStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> GhrothStateManager.useStars(player);
            case ACTIVE_2 -> GhrothStateManager.useJustice(player);
            case CORE -> GhrothStateManager.useNoon(player);
            case PASSIVE -> false;
        };
    }
}
