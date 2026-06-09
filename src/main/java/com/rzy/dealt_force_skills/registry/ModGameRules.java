package com.rzy.dealt_force_skills.registry;

import net.minecraft.world.level.GameRules;

public final class ModGameRules {
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_FORCE_SHOP =
            GameRules.register("dealtforceshop", GameRules.Category.PLAYER, GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_UNDEAD_STEAL =
            GameRules.register("dealtundeadsteal", GameRules.Category.PLAYER, GameRules.BooleanValue.create(true));

    private ModGameRules() {
    }

    public static void register() {
        // Forces static initialization so vanilla /gamerule exposes both mod rules.
    }
}
