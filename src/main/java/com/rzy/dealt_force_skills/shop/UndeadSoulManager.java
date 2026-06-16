package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.undead.UndeadProfession;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncUndeadSouls;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class UndeadSoulManager {
    private static final String SOULS = DealtForceSkillsMod.MODID + ".undead_souls";
    private static final String LAST_SURVIVAL_AWARD_TICK =
            DealtForceSkillsMod.MODID + ".undead_last_survival_award_tick";
    private static final long SURVIVAL_AWARD_INTERVAL_TICKS = 60L * 20L;
    private static final long SURVIVAL_AWARD = 500L;
    private static final long PLAYER_KILL_AWARD = 4500L;
    private static final long PLAYER_DEATH_AWARD = 6000L;

    private UndeadSoulManager() {
    }

    public static long get(ServerPlayer player) {
        return Math.max(0L, player.getPersistentData().getLong(SOULS));
    }

    public static void copy(Player original, Player replacement) {
        CompoundTag from = original.getPersistentData();
        CompoundTag to = replacement.getPersistentData();
        to.putLong(SOULS, Math.max(0L, from.getLong(SOULS)));
        if (from.contains(LAST_SURVIVAL_AWARD_TICK)) {
            to.putLong(LAST_SURVIVAL_AWARD_TICK, from.getLong(LAST_SURVIVAL_AWARD_TICK));
        }
    }

    public static void awardKill(ServerPlayer killer, LivingEntity victim) {
        if (!UndeadStateManager.isUndead(killer)) {
            return;
        }
        long base = victim instanceof Player
                ? PLAYER_KILL_AWARD
                : Math.max(1L, Math.round(victim.getMaxHealth() * 10.0D));
        add(killer, explorerAdjusted(killer, base));
    }

    public static void awardDeath(ServerPlayer player) {
        if (UndeadStateManager.isUndead(player)) {
            add(player, explorerAdjusted(player, PLAYER_DEATH_AWARD));
        }
    }

    public static void awardSurvivalMinute(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        long now = player.level().getGameTime();
        if (!UndeadStateManager.isUndead(player) || player.isCreative() || player.isSpectator()) {
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
        add(player, explorerAdjusted(player, SURVIVAL_AWARD * intervals));
    }

    public static void add(ServerPlayer player, long amount) {
        if (amount <= 0L || player.isCreative() || player.isSpectator()) {
            return;
        }
        set(player, safeAdd(get(player), amount));
    }

    public static long grant(ServerPlayer player, long amount) {
        if (amount > 0L) {
            set(player, safeAdd(get(player), amount));
        }
        return get(player);
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

    public static void sync(ServerPlayer player) {
        NetworkHandler.sendToPlayer(new S2C_SyncUndeadSouls(get(player)), player);
    }

    private static long explorerAdjusted(ServerPlayer player, long amount) {
        return UndeadStateManager.profession(player) == UndeadProfession.EXPLORER
                ? Math.max(1L, Math.round(amount * 1.5D))
                : amount;
    }

    private static void set(ServerPlayer player, long amount) {
        player.getPersistentData().putLong(SOULS, Math.max(0L, amount));
        sync(player);
    }

    private static long safeAdd(long left, long right) {
        long result = left + right;
        return result < 0L ? Long.MAX_VALUE : result;
    }
}
