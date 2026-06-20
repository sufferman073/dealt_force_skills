package com.rzy.dealt_force_skills.registry;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;

public final class ModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_FORCE_SHOP =
            GameRules.register("dealtforceshop", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_UNDEAD_STEAL =
            GameRules.register("dealtundeadsteal", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_BOSSES_ENABLE =
            GameRules.register("dealtbossesenable", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_AGREEXP_UPGRADE =
            GameRules.register("dealtagreexpupgrade", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_UNDEAD_EXTRA_DROP =
            GameRules.register("dealtundeadextradrop", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    private ModGameRules() {
    }

    public static void register() {
        // Forces static initialization so vanilla /gamerule exposes both mod rules.
    }

    public static boolean isExperienceUpgradeEnabled(Player player) {
        return player == null || player.level().getGameRules().getBoolean(DEALT_AGREEXP_UPGRADE);
    }

    public static int effectiveExperienceLevel(Player player) {
        return isExperienceUpgradeEnabled(player) ? Math.max(0, player.experienceLevel) : 0;
    }
}
