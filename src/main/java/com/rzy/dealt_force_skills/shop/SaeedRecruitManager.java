package com.rzy.dealt_force_skills.shop;

import com.rzy.dealt_force_skills.character.saeed.SaeedGuardType;
import com.rzy.dealt_force_skills.character.saeed.SaeedStateManager;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_OpenSaeedMonitorScreen;
import com.rzy.dealt_force_skills.network.S2C_OpenSaeedRecruitScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public final class SaeedRecruitManager {
    private SaeedRecruitManager() {
    }

    public static boolean shouldOpen(ServerPlayer player) {
        return !player.isCreative()
                && !player.isSpectator()
                && SaeedStateManager.isSaeed(player);
    }

    public static void open(ServerPlayer player) {
        if (!shouldOpen(player)) {
            return;
        }
        SaeedStateManager.initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_OpenSaeedRecruitScreen(SaeedStateManager.needsInitialGuardChoice(player)
                ? SaeedStateManager.initialGuardData(player)
                : SaeedStateManager.shopData(player)), player);
    }

    public static void openInitialChoiceIfNeeded(ServerPlayer player) {
        SaeedStateManager.initializeIfNeeded(player);
        if (SaeedStateManager.needsInitialGuardChoice(player) && !player.isSpectator()) {
            NetworkHandler.sendToPlayer(new S2C_OpenSaeedRecruitScreen(SaeedStateManager.initialGuardData(player)), player);
        }
    }

    public static void openMonitor(ServerPlayer player) {
        if (!shouldOpen(player)) {
            return;
        }
        SaeedStateManager.initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_OpenSaeedMonitorScreen(SaeedStateManager.monitorData(player)), player);
    }

    public static void handleAction(ServerPlayer player, SaeedRecruitAction action, String guardTypeId) {
        if (action == SaeedRecruitAction.CHOOSE_INITIAL_GUARD) {
            if (!SaeedStateManager.isSaeed(player) || player.isSpectator()) {
                return;
            }
        } else if (!shouldOpen(player)) {
            return;
        }
        SaeedStateManager.initializeIfNeeded(player);
        switch (action) {
            case RECRUIT -> SaeedGuardType.byId(guardTypeId).ifPresentOrElse(
                    type -> SaeedStateManager.recruitGuard(player, type, false),
                    () -> player.displayClientMessage(Component.translatable(
                            "message.dealt_force_skills.saeed_recruit.invalid_guard"), true));
            case CHOOSE_INITIAL_GUARD -> SaeedGuardType.byId(guardTypeId).ifPresentOrElse(
                    type -> SaeedStateManager.chooseInitialGuard(player, type),
                    () -> player.displayClientMessage(Component.translatable(
                            "message.dealt_force_skills.saeed_recruit.invalid_guard"), true));
            case UPGRADE_PRESTIGE -> SaeedStateManager.upgradePrestige(player);
            case TOGGLE_ATTACK -> SaeedStateManager.toggleAttack(player);
            case TOGGLE_BREAK_BLOCKS -> SaeedStateManager.toggleBreakBlocks(player);
            case TOGGLE_INTERACT -> SaeedStateManager.toggleInteract(player);
            case TOGGLE_FOLLOW -> SaeedStateManager.toggleFollow(player);
            case TOGGLE_FRIENDLY_FIRE -> SaeedStateManager.toggleFriendlyFire(player);
            case WITHDRAW_ALL -> SaeedStateManager.withdrawAll(player, SaeedStateManager.Refund.NORMAL);
        }
        open(player);
    }
}
