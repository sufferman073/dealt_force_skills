package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.lexninjia.LexNinjiaStateManager;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncHaffCoins;
import com.rzy.dealt_force_skills.registry.ModGameRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class HaffCoinManager {
    private static final String COINS = DealtForceSkillsMod.MODID + ".haff_coins";
    private static final String LAST_SURVIVAL_AWARD_TICK = DealtForceSkillsMod.MODID + ".haff_last_survival_award_tick";
    private static final long SURVIVAL_AWARD_INTERVAL_TICKS = 60L * 20L;
    private static final long SURVIVAL_AWARD = 500L;
    private static final long PLAYER_KILL_AWARD = 4500L;
    private static final long PLAYER_DEATH_AWARD = 6000L;

    private HaffCoinManager() {
    }

    public static long get(ServerPlayer player) {
        return Math.max(0L, player.getPersistentData().getLong(COINS));
    }

    public static void copy(Player original, Player replacement) {
        CompoundTag from = original.getPersistentData();
        CompoundTag to = replacement.getPersistentData();
        to.putLong(COINS, Math.max(0L, from.getLong(COINS)));
        if (from.contains(LAST_SURVIVAL_AWARD_TICK)) {
            to.putLong(LAST_SURVIVAL_AWARD_TICK, from.getLong(LAST_SURVIVAL_AWARD_TICK));
        }
    }

    public static boolean canUseShop(ServerPlayer player) {
        return !player.isCreative() && !player.isSpectator();
    }

    public static boolean canEarnCoins(ServerPlayer player) {
        return canUseShop(player)
                && !UndeadStateManager.isUndead(player)
                && !LexNinjiaStateManager.isLexNinjia(player)
                && player.serverLevel().getGameRules().getBoolean(ModGameRules.DEALT_FORCE_SHOP);
    }

    public static void add(ServerPlayer player, long amount) {
        if (amount <= 0L || !canEarnCoins(player)) {
            return;
        }
        set(player, safeAdd(get(player), amount));
    }

    public static boolean trySpend(ServerPlayer player, long amount) {
        if (amount <= 0L) {
            return true;
        }
        long current = get(player);
        if (current < amount) {
            sync(player);
            return false;
        }
        set(player, current - amount);
        return true;
    }

    public static void awardKill(ServerPlayer killer, LivingEntity victim) {
        if (victim instanceof Player) {
            add(killer, PLAYER_KILL_AWARD);
            return;
        }
        add(killer, Math.max(1L, Math.round(victim.getMaxHealth() * 10.0D)));
    }

    public static void awardDeath(ServerPlayer player) {
        add(player, PLAYER_DEATH_AWARD);
    }

    public static void awardSurvivalMinute(ServerPlayer player) {
        long now = player.level().getGameTime();
        CompoundTag tag = player.getPersistentData();
        if (!canEarnCoins(player)) {
            tag.putLong(LAST_SURVIVAL_AWARD_TICK, now);
            return;
        }
        if (!tag.contains(LAST_SURVIVAL_AWARD_TICK)) {
            tag.putLong(LAST_SURVIVAL_AWARD_TICK, now);
            return;
        }
        long last = tag.getLong(LAST_SURVIVAL_AWARD_TICK);
        if (now < last || now - last < SURVIVAL_AWARD_INTERVAL_TICKS) {
            return;
        }
        long intervals = Math.max(1L, (now - last) / SURVIVAL_AWARD_INTERVAL_TICKS);
        tag.putLong(LAST_SURVIVAL_AWARD_TICK, last + intervals * SURVIVAL_AWARD_INTERVAL_TICKS);
        add(player, SURVIVAL_AWARD * intervals);
    }

    public static void sync(ServerPlayer player) {
        NetworkHandler.sendToPlayer(new S2C_SyncHaffCoins(get(player)), player);
    }

    private static void set(ServerPlayer player, long amount) {
        player.getPersistentData().putLong(COINS, Math.max(0L, amount));
        sync(player);
    }

    private static long safeAdd(long left, long right) {
        long result = left + right;
        return result < 0L ? Long.MAX_VALUE : result;
    }
}
