package com.rzy.dealt_force_skills.util;

import com.rzy.dealt_force_skills.character.department.DepartmentOfTransportationStateManager;
import com.rzy.dealt_force_skills.character.undead.UndeadStateManager;
import com.rzy.dealt_force_skills.team.DealtTeamManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Shared hostile / friendly targeting helpers.
 * <p>
 * For skills that must hurt or CC the caster (grenades, fields, traps, etc.),
 * use {@link #isSelfOrHostileLivingFor(Entity, LivingEntity)} /
 * {@link #isSelfOrHostilePlayerFor(Entity, Player)}. Teammates stay protected.
 * Design inventory:
 * {@code characters_records/self_harm_skills.txt}
 */
public final class TargetingUtil {
    private TargetingUtil() {
    }

    public static boolean isTargetablePlayer(Player player) {
        return player.isAlive()
                && !player.isSpectator()
                && !player.getAbilities().instabuild
                && !DepartmentOfTransportationStateManager.isFullyConcealed(player)
                && (!(player instanceof ServerPlayer serverPlayer)
                || !UndeadStateManager.blocksIncomingAttack(serverPlayer));
    }

    public static boolean isTargetableLiving(LivingEntity entity) {
        return entity.isAlive() && (!(entity instanceof Player player) || isTargetablePlayer(player));
    }

    /**
     * Hostile-only: skips self and teammates.
     * Prefer {@link #isSelfOrHostileLivingFor} when the skill is listed as self-harming.
     */
    public static boolean isHostilePlayerFor(Entity owner, Player target) {
        return isTargetablePlayer(target) && !shouldSkipFriendlyControl(owner, target);
    }

    /**
     * Hostile-only: skips self and teammates.
     * Prefer {@link #isSelfOrHostileLivingFor} when the skill is listed as self-harming.
     */
    public static boolean isHostileLivingFor(Entity owner, LivingEntity target) {
        return isTargetableLiving(target) && !shouldSkipFriendlyControl(owner, target);
    }

    /**
     * Central self-harm targeting: allows the caster and hostiles, blocks teammates.
     * Use for skills that can damage/control the user (see characters_records list).
     */
    public static boolean isSelfOrHostilePlayerFor(Entity owner, Player target) {
        return isTargetablePlayer(target)
                && (isSelf(owner, target) || !shouldSkipFriendlyExceptSelf(owner, target));
    }

    /**
     * Central self-harm targeting: allows the caster and hostiles, blocks teammates.
     * Use for skills that can damage/control the user (see characters_records list).
     */
    public static boolean isSelfOrHostileLivingFor(Entity owner, LivingEntity target) {
        return isTargetableLiving(target)
                && (isSelf(owner, target) || !shouldSkipFriendlyExceptSelf(owner, target));
    }

    public static boolean isSelf(Entity owner, Entity target) {
        return owner != null && target != null && owner.getUUID().equals(target.getUUID());
    }

    /**
     * True when player-sourced harmful control should skip the target (self or teammate).
     * For self-damaging skills, do not use this alone - use {@link #shouldSkipFriendlyExceptSelf}.
     */
    public static boolean shouldSkipFriendlyControl(Entity owner, LivingEntity target) {
        if (!(owner instanceof Player) || !(target instanceof Player)) {
            return false;
        }
        return owner.getUUID().equals(target.getUUID()) || DealtTeamManager.areTeammates(owner, target);
    }

    /**
     * Team-safe filter for self-harming skills: only teammates are skipped; self is allowed.
     */
    public static boolean shouldSkipFriendlyExceptSelf(Entity owner, LivingEntity target) {
        if (!(owner instanceof Player) || !(target instanceof Player)) {
            return false;
        }
        if (owner.getUUID().equals(target.getUUID())) {
            return false;
        }
        return DealtTeamManager.areTeammates(owner, target);
    }
}
