package com.rzy.dealt_force_skills.compat;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.team.DealtTeamManager;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class PlayerReviveCompatEvents {
    private PlayerReviveCompatEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerReviveInteract(PlayerInteractEvent.EntityInteract event) {
        if (!PlayerReviveCompat.isLoaded() || !(event.getEntity() instanceof ServerPlayer helper)) {
            return;
        }
        Entity targetEntity = event.getTarget();
        if (!(targetEntity instanceof ServerPlayer target) || !PlayerReviveCompat.isBleeding(target)) {
            return;
        }
        if (helper == target || DealtTeamManager.canRescue(helper, target)) {
            return;
        }
        event.setCanceled(true);
        PlayerReviveCompat.removePlayerAsHelper(helper);
        helper.displayClientMessage(Component.translatable(
                "message.dealt_force_skills.playerrevive.not_teammate_helper"), true);
        target.displayClientMessage(Component.translatable(
                "message.dealt_force_skills.playerrevive.not_teammate_target", helper.getDisplayName()), true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerReviveTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            PlayerReviveCompat.finishNativeReviveBonuses(player);
            PlayerReviveCompat.boostReviveProgress(player);
        }
    }

    @SubscribeEvent
    public static void onServerTickTeamWipe(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            PlayerReviveCompat.tickTeamWipe(server);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerReviveTickBefore(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.START || event.player.level().isClientSide) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            PlayerReviveCompat.trackNativeReviveHelpers(player);
        }
    }
}
