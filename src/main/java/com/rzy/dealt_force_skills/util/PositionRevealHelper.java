package com.rzy.dealt_force_skills.util;

import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.team.DealtTeamManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

/**
 * Shared position-reveal helpers:
 * <ul>
 *   <li>share reveal packets from a caster to their online teammates</li>
 *   <li>filter recon targets so teammates are never self-revealed as hostiles</li>
 *   <li>continuous ally mutual exposure is handled by
 *       {@link com.rzy.dealt_force_skills.team.TeammateRevealSync} +
 *       {@link com.rzy.dealt_force_skills.client.ClientTeammateRevealState}</li>
 * </ul>
 */
public final class PositionRevealHelper {
    private PositionRevealHelper() {
    }

    public static boolean isValidReconTarget(ServerPlayer caster, LivingEntity target) {
        if (target == null || !target.isAlive() || caster == null) {
            return false;
        }
        if (target == caster || target.getUUID().equals(caster.getUUID())) {
            return false;
        }
        // Recon skills must not expose teammates to the caster (prevents friend/foe misreads).
        return !(target instanceof ServerPlayer teammateCandidate
                && DealtTeamManager.areTeammates(caster, teammateCandidate));
    }

    public static List<LivingEntity> filterReconTargets(ServerPlayer caster, Collection<? extends LivingEntity> targets) {
        List<LivingEntity> filtered = new ArrayList<>();
        for (LivingEntity target : targets) {
            if (isValidReconTarget(caster, target)) {
                filtered.add(target);
            }
        }
        return filtered;
    }

    /**
     * Send a client packet to the caster and all online teammates (same payload).
     */
    public static <T> void sendToCasterAndTeammates(ServerPlayer caster, T packet) {
        if (caster == null || packet == null) {
            return;
        }
        NetworkHandler.sendToPlayer(packet, caster);
        for (ServerPlayer teammate : DealtTeamManager.onlineTeammates(caster)) {
            NetworkHandler.sendToPlayer(packet, teammate);
        }
    }

    /**
     * Build and send a packet for the caster and each teammate (allows per-receiver customization).
     */
    public static <T> void sendToCasterAndTeammates(ServerPlayer caster, Function<ServerPlayer, T> packetFactory) {
        if (caster == null || packetFactory == null) {
            return;
        }
        T packet = packetFactory.apply(caster);
        if (packet != null) {
            NetworkHandler.sendToPlayer(packet, caster);
        }
        for (ServerPlayer teammate : DealtTeamManager.onlineTeammates(caster)) {
            T teammatePacket = packetFactory.apply(teammate);
            if (teammatePacket != null) {
                NetworkHandler.sendToPlayer(teammatePacket, teammate);
            }
        }
    }

    public static void shareEntityGlowToTeammates(ServerPlayer caster, Entity target, boolean glowing) {
        if (caster == null || target == null || !(target instanceof LivingEntity living)) {
            return;
        }
        // Server-side glow is visible to everyone; prefer client packets via sendToCasterAndTeammates.
        // This helper only applies when the existing system already uses setGlowingTag for the caster view.
        if (!glowing) {
            return;
        }
        // No-op server glow share: keep client-only outlines for ally visibility.
    }
}
