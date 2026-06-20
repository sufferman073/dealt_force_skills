package com.rzy.dealt_force_skills.character.stinger;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncStingerState;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class StingerStateManager {
    public static final int SMOKE_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.smoke_cooldown_ticks", 40 * 20);
    public static final int DRONE_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.drone_cooldown_ticks", 55 * 20);
    public static final int STIM_MAX_CHARGES = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.stim_max_charges", 6);
    public static final int STIM_RECHARGE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.stim_recharge_ticks", 25 * 20);
    public static final int STIM_DURATION_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.stim_duration_ticks", 20 * 20);
    public static final int DOWNED_DURATION_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.downed_duration_ticks", 45 * 20);
    public static final int DOWNED_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.downed_cooldown_ticks", 60 * 20);
    public static final int REVIVE_OTHER_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.revive_other_ticks", 3 * 20);
    public static final int REVIVE_SELF_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.revive_self_ticks", 20);
    public static final double REVIVE_RANGE = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.stinger.stinger_state_manager.revive_range", 1.5D);
    private static final DustParticleOptions DOWNED_DUST = new DustParticleOptions(new Vector3f(0.28f, 0.86f, 1.0f), 1.25f);

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".stinger";
    private static final String INITIALIZED = "Initialized";
    private static final String SMOKE_COOLDOWN_UNTIL = "SmokeCooldownUntil";
    private static final String DRONE_COOLDOWN_UNTIL = "DroneCooldownUntil";
    private static final String STIM_CHARGES = "StimCharges";
    private static final String STIM_NEXT_RECHARGE = "StimNextRecharge";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String STIM_MODE = "StimMode";
    private static final String RESCUE_TARGET_ID = "RescueTargetId";
    private static final String RESCUE_TICKS = "RescueTicks";
    private static final String SELF_RESCUE_TICKS = "SelfRescueTicks";
    private static final String DOWNED_UNTIL = "DownedUntil";
    private static final String DOWNED_COOLDOWN_UNTIL = "DownedCooldownUntil";
    private static final String EXECUTING_DOWNED_DEATH = "ExecutingDownedDeath";

    private StingerStateManager() {
    }

    public static boolean isStinger(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.STINGER_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isStinger(player)) {
            return;
        }

        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }

        tag.putBoolean(INITIALIZED, true);
        tag.putLong(SMOKE_COOLDOWN_UNTIL, 0L);
        tag.putLong(DRONE_COOLDOWN_UNTIL, 0L);
        tag.putInt(STIM_CHARGES, STIM_MAX_CHARGES);
        tag.putLong(STIM_NEXT_RECHARGE, 0L);
        tag.putInt(EQUIPPED_TOOL, StingerTool.NONE.ordinal());
        tag.putInt(STIM_MODE, StingerStimMode.HEAL.ordinal());
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        tag.putInt(SELF_RESCUE_TICKS, 0);
    }

    public static void copyState(Player original, Player target) {
        CompoundTag originalData = original.getPersistentData();
        if (originalData.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            target.getPersistentData().put(ROOT_TAG, originalData.getCompound(ROOT_TAG).copy());
        }
    }

    public static void clearState(Player player) {
        player.getPersistentData().remove(ROOT_TAG);
    }

    public static void tick(ServerPlayer player) {
        if (!isStinger(player)) {
            return;
        }
        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        rechargeStim(player, now);
        tickRescue(player);
        tickDowned(player);
    }

    public static void tickDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        long until = tag.getLong(DOWNED_UNTIL);
        if (until <= 0L) {
            if (player.hasEffect(ModEffects.STINGER_DOWNED.get())) {
                player.removeEffect(ModEffects.STINGER_DOWNED.get());
            }
            return;
        }

        int remaining = (int) Math.min(Integer.MAX_VALUE, until - player.level().getGameTime());
        if (remaining <= 0) {
            expireDowned(player);
            return;
        }

        MobEffectInstance current = player.getEffect(ModEffects.STINGER_DOWNED.get());
        if (current == null || current.getDuration() < remaining - 5) {
            player.addEffect(new MobEffectInstance(ModEffects.STINGER_DOWNED.get(),
                    remaining, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.stinger_downed.0.amplifier", 0), false, true, true));
        }
        if (player.getHealth() < 1.0f) {
            player.setHealth(1.0f);
        }
        player.setSprinting(false);
        player.stopUsingItem();
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        spawnDownedParticles(player);
    }

    public static boolean shouldEnterDowned(ServerPlayer player, float incomingDamage) {
        return incomingDamage >= player.getHealth() && canEnterDowned(player);
    }

    public static boolean canEnterDowned(ServerPlayer player) {
        return !isDowned(player)
                && !isExecutingDownedDeath(player)
                && downedCooldownReady(player)
                && hasStingerProtection(player);
    }

    public static void enterDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        long until = now + DOWNED_DURATION_TICKS;
        tag.putLong(DOWNED_UNTIL, until);
        tag.putLong(DOWNED_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, DOWNED_COOLDOWN_TICKS));
        tag.putInt(SELF_RESCUE_TICKS, 0);
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        player.setHealth(Math.max(1.0f, Math.min(player.getHealth(), player.getMaxHealth())));
        player.clearFire();
        player.addEffect(new MobEffectInstance(ModEffects.STINGER_DOWNED.get(),
                DOWNED_DURATION_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.stinger_downed.1.amplifier", 0), false, true, true));
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.downed"), true);
    }

    public static void clearDowned(ServerPlayer player, boolean fullHeal) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        player.removeEffect(ModEffects.STINGER_DOWNED.get());
        if (fullHeal) {
            player.setHealth(player.getMaxHealth());
            player.clearFire();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.regeneration.2.duration_ticks", 4 * 20), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.regeneration.2.amplifier", 0), false, true, true));
        }
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.revived"), true);
    }

    public static void clearDownedOnDeath(ServerPlayer player) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        player.removeEffect(ModEffects.STINGER_DOWNED.get());
    }

    public static boolean isDowned(Player player) {
        return data(player).getLong(DOWNED_UNTIL) > player.level().getGameTime();
    }

    public static boolean isExecutingDownedDeath(Player player) {
        return data(player).getBoolean(EXECUTING_DOWNED_DEATH);
    }

    public static int downedRemainingTicks(Player player) {
        long remaining = data(player).getLong(DOWNED_UNTIL) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
    }

    public static boolean hasStingerProtection(Player player) {
        return player.hasEffect(ModEffects.STINGER_STIM_HEAL.get())
                || player.hasEffect(ModEffects.STINGER_SMOKE_REGEN.get());
    }

    public static boolean smokeReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(SMOKE_COOLDOWN_UNTIL);
    }

    public static int smokeCooldownRemainingTicks(Player player) {
        return remainingTicks(player, SMOKE_COOLDOWN_UNTIL);
    }

    public static boolean consumeSmoke(ServerPlayer player) {
        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        if (now < tag.getLong(SMOKE_COOLDOWN_UNTIL)) {
            return false;
        }
        tag.putLong(SMOKE_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, SMOKE_COOLDOWN_TICKS));
        return true;
    }

    public static boolean droneReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(DRONE_COOLDOWN_UNTIL);
    }

    public static int droneCooldownRemainingTicks(Player player) {
        return remainingTicks(player, DRONE_COOLDOWN_UNTIL);
    }

    public static boolean consumeDrone(ServerPlayer player) {
        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        if (now < tag.getLong(DRONE_COOLDOWN_UNTIL)) {
            return false;
        }
        tag.putLong(DRONE_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, DRONE_COOLDOWN_TICKS));
        return true;
    }

    public static int stimCharges(Player player) {
        return data(player).getInt(STIM_CHARGES);
    }

    public static int stimRechargeRemainingTicks(Player player) {
        if (stimCharges(player) >= STIM_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, STIM_NEXT_RECHARGE);
    }

    public static boolean consumeStimCharge(ServerPlayer player) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(STIM_CHARGES);
        if (charges <= 0) {
            return false;
        }
        tag.putInt(STIM_CHARGES, charges - 1);
        if (charges - 1 < STIM_MAX_CHARGES && tag.getLong(STIM_NEXT_RECHARGE) <= 0L) {
            tag.putLong(STIM_NEXT_RECHARGE,
                    SkillCooldownHelper.until(player, player.level().getGameTime(), STIM_RECHARGE_TICKS));
        }
        return true;
    }

    public static StingerTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        StingerTool[] tools = StingerTool.values();
        return ordinal >= 0 && ordinal < tools.length ? tools[ordinal] : StingerTool.NONE;
    }

    public static void setEquippedTool(Player player, StingerTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
    }

    public static StingerStimMode stimMode(Player player) {
        int ordinal = data(player).getInt(STIM_MODE);
        StingerStimMode[] modes = StingerStimMode.values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : StingerStimMode.HEAL;
    }

    public static void toggleStimMode(Player player) {
        StingerStimMode next = stimMode(player) == StingerStimMode.HEAL
                ? StingerStimMode.SUPPRESS
                : StingerStimMode.HEAL;
        data(player).putInt(STIM_MODE, next.ordinal());
    }

    public static int rescueTicks(Player player) {
        return Math.max(data(player).getInt(RESCUE_TICKS), data(player).getInt(SELF_RESCUE_TICKS));
    }

    public static int rescueRequiredTicks(Player player) {
        if (isDowned(player)) {
            return REVIVE_SELF_TICKS;
        }
        return data(player).getInt(RESCUE_TICKS) > 0 ? REVIVE_OTHER_TICKS : 0;
    }

    public static void applyStimHeal(LivingEntity target) {
        target.addEffect(new MobEffectInstance(ModEffects.STINGER_STIM_HEAL.get(),
                STIM_DURATION_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.stinger_stim_heal.3.amplifier", 0), false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                STIM_DURATION_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.regeneration.4.amplifier", 1), false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,
                STIM_DURATION_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.absorption.5.amplifier", 1), false, true, true));
        for (MobEffectInstance effect : new ArrayList<>(target.getActiveEffects())) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                target.removeEffect(effect.getEffect());
            }
        }
    }

    public static void applySmokeRegen(ServerPlayer target) {
        target.addEffect(new MobEffectInstance(ModEffects.STINGER_SMOKE_REGEN.get(),
                com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.stinger_smoke_regen.6.duration_ticks", 30), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.stinger_smoke_regen.6.amplifier", 0), false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.regeneration.7.duration_ticks", 30), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.regeneration.7.amplifier", 0), false, true, true));
    }

    public static void applyStimSuppression(LivingEntity target) {
        target.addEffect(new MobEffectInstance(ModEffects.STINGER_STIM_SUPPRESSION.get(),
                STIM_DURATION_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.stinger.stinger_state_manager.effect.stinger_stim_suppression.8.amplifier", 0), false, true, true));
    }

    public static boolean hasStimSuppression(LivingEntity target) {
        return target.hasEffect(ModEffects.STINGER_STIM_SUPPRESSION.get());
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isStinger(player)) {
            return;
        }
        initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_SyncStingerState(
                smokeCooldownRemainingTicks(player),
                droneCooldownRemainingTicks(player),
                stimCharges(player),
                STIM_MAX_CHARGES,
                stimRechargeRemainingTicks(player),
                equippedTool(player).ordinal(),
                stimMode(player).ordinal(),
                rescueTicks(player),
                rescueRequiredTicks(player),
                downedRemainingTicks(player),
                downedMarkers(player)
        ), player);
    }

    private static void rechargeStim(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(STIM_CHARGES);
        if (charges >= STIM_MAX_CHARGES) {
            tag.putLong(STIM_NEXT_RECHARGE, 0L);
            return;
        }
        long next = tag.getLong(STIM_NEXT_RECHARGE);
        if (next <= 0L) {
            tag.putLong(STIM_NEXT_RECHARGE, SkillCooldownHelper.until(player, now, STIM_RECHARGE_TICKS));
            return;
        }
        while (charges < STIM_MAX_CHARGES && now >= next) {
            charges++;
            next += SkillCooldownHelper.ticks(player, STIM_RECHARGE_TICKS);
        }
        tag.putInt(STIM_CHARGES, charges);
        tag.putLong(STIM_NEXT_RECHARGE, charges >= STIM_MAX_CHARGES ? 0L : next);
    }

    private static void tickRescue(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (!player.isShiftKeyDown()) {
            resetRescue(tag);
            return;
        }

        if (isDowned(player)) {
            int ticks = tag.getInt(SELF_RESCUE_TICKS) + 1;
            tag.putInt(SELF_RESCUE_TICKS, ticks);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.self_rescue",
                    progressBar(ticks, REVIVE_SELF_TICKS)), true);
            if (ticks >= REVIVE_SELF_TICKS) {
                clearDowned(player, true);
            }
            return;
        }

        ServerPlayer target = nearestDownedPlayer(player);
        if (target == null) {
            resetRescue(tag);
            return;
        }

        int targetId = target.getId();
        int ticks = tag.getInt(RESCUE_TARGET_ID) == targetId ? tag.getInt(RESCUE_TICKS) + 1 : 1;
        tag.putInt(RESCUE_TARGET_ID, targetId);
        tag.putInt(RESCUE_TICKS, ticks);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.rescuing",
                target.getDisplayName(), progressBar(ticks, REVIVE_OTHER_TICKS)), true);
        if (ticks >= REVIVE_OTHER_TICKS) {
            clearDowned(target, true);
            resetRescue(tag);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.rescue_done",
                    target.getDisplayName()), true);
        }
    }

    private static ServerPlayer nearestDownedPlayer(ServerPlayer player) {
        double rangeSqr = REVIVE_RANGE * REVIVE_RANGE;
        return player.server.getPlayerList().getPlayers().stream()
                .filter(target -> target != player && target.level() == player.level())
                .filter(target -> TargetingUtil.isTargetablePlayer(target) && isDowned(target))
                .filter(target -> target.distanceToSqr(player) <= rangeSqr)
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static List<StingerDownedMarker> downedMarkers(ServerPlayer stinger) {
        List<StingerDownedMarker> markers = new ArrayList<>();
        for (ServerPlayer player : stinger.server.getPlayerList().getPlayers()) {
            if (player.level() == stinger.level() && isDowned(player)) {
                markers.add(new StingerDownedMarker(player.getId(), player.position(), downedRemainingTicks(player)));
            }
        }
        markers.sort(Comparator.comparingDouble(marker -> marker.position().distanceToSqr(stinger.position())));
        return markers;
    }

    private static boolean downedCooldownReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(DOWNED_COOLDOWN_UNTIL);
    }

    private static void expireDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putBoolean(EXECUTING_DOWNED_DEATH, true);
        player.removeEffect(ModEffects.STINGER_DOWNED.get());
        try {
            player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
        } finally {
            tag.remove(EXECUTING_DOWNED_DEATH);
        }
    }

    private static void spawnDownedParticles(ServerPlayer player) {
        if (player.tickCount % 4 != 0 || !(player.level() instanceof ServerLevel level)) {
            return;
        }
        level.sendParticles(DOWNED_DUST,
                player.getX(),
                player.getY() + 1.05D,
                player.getZ(),
                8,
                0.42D,
                0.55D,
                0.42D,
                0.0D);
    }

    private static void resetRescue(CompoundTag tag) {
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        tag.putInt(SELF_RESCUE_TICKS, 0);
    }

    private static String progressBar(int ticks, int required) {
        int filled = Math.max(0, Math.min(10, ticks * 10 / Math.max(1, required)));
        return "[" + "#".repeat(filled) + ".".repeat(10 - filled) + "]";
    }

    private static int remainingTicks(Player player, String key) {
        long remaining = data(player).getLong(key) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
    }

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }
}
