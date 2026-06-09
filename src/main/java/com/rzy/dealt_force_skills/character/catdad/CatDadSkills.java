package com.rzy.dealt_force_skills.character.catdad;

import com.rzy.dealt_force_skills.character.SkillSlot;
import net.minecraft.server.level.ServerPlayer;

public final class CatDadSkills {
    private CatDadSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        CatDadStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> CatDadStateManager.useHiss(player);
            case ACTIVE_2 -> CatDadStateManager.useBlock(player);
            case CORE -> alternate ? CatDadStateManager.toggleTruckBreakBlocks(player) : CatDadStateManager.useCore(player);
            case PASSIVE -> false;
        };
    }
}
