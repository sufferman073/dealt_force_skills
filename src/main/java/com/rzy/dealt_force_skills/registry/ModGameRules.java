package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.character.CharacterSkinSync;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;

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
    public static final GameRules.Key<GameRules.BooleanValue> DELTA_SKINS =
            GameRules.register("deltaskins", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true, (server, value) -> CharacterSkinSync.syncAll(server)));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_NOX_INVISIBLE =
            GameRules.register("dealtnoxinvisible", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_ARMOR_SPECIAL =
            GameRules.register("dealtarmorspecial", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_WOUND =
            GameRules.register("dealtwound", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_LIMBS_DAMAGE =
            GameRules.register("dealtlimbsdamage", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_GAMBLER =
            GameRules.register("dealtgambler", GameRules.Category.MOBS,
                    GameRules.BooleanValue.create(false));
    public static final GameRules.Key<GameRules.BooleanValue> DEALT_BREAKABEL =
            GameRules.register("dealtbreakabel", GameRules.Category.PLAYER,
                    GameRules.BooleanValue.create(true));

    private ModGameRules() {
    }

    public static void register() {
        // Forces static initialization so vanilla /gamerule exposes both mod rules.
    }

    public static boolean isExperienceUpgradeEnabled(Player player) {
        return player == null || player.level().getGameRules().getBoolean(DEALT_AGREEXP_UPGRADE);
    }

    public static int effectiveExperienceLevel(Player player) {
        if (player == null) {
            return 0;
        }
        return isExperienceUpgradeEnabled(player) ? Math.max(0, player.experienceLevel) : 0;
    }

    public static boolean areCharacterSkinsEnabled(Player player) {
        return player == null || player.level().getGameRules().getBoolean(DELTA_SKINS);
    }

    public static boolean isNoxInvisibilityEnabled(Player player) {
        return player == null || player.level().getGameRules().getBoolean(DEALT_NOX_INVISIBLE);
    }

    public static boolean areArmorSpecialsEnabled(Player player) {
        return player == null || player.level().getGameRules().getBoolean(DEALT_ARMOR_SPECIAL);
    }

    public static boolean areWoundsEnabled(Player player) {
        return player != null && player.level().getGameRules().getBoolean(DEALT_WOUND);
    }

    public static boolean areLimbsDamageReductionsEnabled(Player player) {
        return player == null || player.level().getGameRules().getBoolean(DEALT_LIMBS_DAMAGE);
    }

    public static boolean areSkillBlockBreaksEnabled(Level level) {
        return level == null || level.getGameRules().getBoolean(DEALT_BREAKABEL);
    }
}
