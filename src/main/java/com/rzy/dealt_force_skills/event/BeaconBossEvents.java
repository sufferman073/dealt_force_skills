package com.rzy.dealt_force_skills.event;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.boss.BeaconSummonManager;
import com.rzy.dealt_force_skills.boss.FearManager;
import com.rzy.dealt_force_skills.entity.BeaconBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.entity.player.SleepingTimeCheckEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID)
public final class BeaconBossEvents {
    private BeaconBossEvents() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.level().isClientSide) {
            return;
        }
        if (event.player instanceof ServerPlayer player) {
            FearManager.tick(player);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) {
            return;
        }
        if (event.level instanceof ServerLevel serverLevel) {
            BeaconSummonManager.serverTick(serverLevel);
        }
    }

    /**
     * Fear / injury apply on attack connect, even if later damage is fully absorbed (shield / armor).
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.level().isClientSide) {
            return;
        }
        BeaconBossEntity boss = beaconAttacker(event.getSource());
        if (boss != null && boss.isAlive()) {
            boss.onAttackConnected(player);
        }
    }

    private static BeaconBossEntity beaconAttacker(DamageSource source) {
        if (source == null) {
            return null;
        }
        Entity entity = source.getEntity();
        if (entity instanceof BeaconBossEntity boss) {
            return boss;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof BeaconBossEntity boss) {
            return boss;
        }
        if (direct instanceof Projectile projectile && projectile.getOwner() instanceof BeaconBossEntity boss) {
            return boss;
        }
        return null;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FearManager.onPlayerDeath(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!(event.getOriginal() instanceof ServerPlayer original)
                || !(event.getEntity() instanceof ServerPlayer clone)) {
            return;
        }
        if (event.isWasDeath()) {
            FearManager.copyOnClone(original, clone);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            FearManager.setStacks(player, FearManager.getStacks(player), false);
        }
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (FearManager.blocksSleep(event.getEntity())) {
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
            event.getEntity().displayClientMessage(Component.translatable(
                    "message.dealt_force_skills.beacon_boss.fear_sleep"), true);
        }
    }

    @SubscribeEvent
    public static void onSleepTimeCheck(SleepingTimeCheckEvent event) {
        if (FearManager.blocksSleep(event.getEntity())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
