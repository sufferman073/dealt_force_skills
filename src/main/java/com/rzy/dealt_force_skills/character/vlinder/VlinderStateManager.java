package com.rzy.dealt_force_skills.character.vlinder;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.entity.VlinderActiveDefenseDroneEntity;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncVlinderState;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class VlinderStateManager {
    public static final int MEDICAL_MAX_CHARGES = 2;
    public static final int MEDICAL_RECHARGE_TICKS = 40 * 20;
    public static final int SMOKE_MAX_CHARGES = 2;
    public static final int SMOKE_RECHARGE_TICKS = 40 * 20;
    public static final int CORE_COOLDOWN_TICKS = 100 * 20;
    public static final int HEALING_DUST_TICKS = 20 * 20;
    public static final int MEDICAL_WASTE_TICKS = 20 * 20;
    public static final int DOWNED_DURATION_TICKS = 90 * 20;
    public static final int RESCUE_PROTECTION_TICKS = 60 * 20;
    public static final int REVIVE_OTHER_TICKS = 5 * 20;
    public static final int REVIVE_SELF_TICKS = 3 * 20;
    public static final double REVIVE_RANGE = 1.5D;
    public static final double LOCK_RANGE = 96.0D;

    private static final DustParticleOptions DOWNED_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.35f, 0.72f), 1.25f);
    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".vlinder";
    private static final String INITIALIZED = "Initialized";
    private static final String MEDICAL_CHARGES = "MedicalCharges";
    private static final String MEDICAL_NEXT_RECHARGE = "MedicalNextRecharge";
    private static final String SMOKE_CHARGES = "SmokeCharges";
    private static final String SMOKE_NEXT_RECHARGE = "SmokeNextRecharge";
    private static final String CORE_COOLDOWN_UNTIL = "CoreCooldownUntil";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String DRONE_MODE = "DroneMode";
    private static final String LOCKED_TARGET_ID = "LockedTargetId";
    private static final String RESCUE_TARGET_ID = "RescueTargetId";
    private static final String RESCUE_TICKS = "RescueTicks";
    private static final String SELF_RESCUE_TICKS = "SelfRescueTicks";
    private static final String DOWNED_UNTIL = "DownedUntil";
    private static final String RESCUE_PROTECTION_UNTIL = "RescueProtectionUntil";
    private static final String VLINDER_BUFF_UNTIL = "VlinderBuffUntil";
    private static final String EXECUTING_DOWNED_DEATH = "ExecutingDownedDeath";

    private VlinderStateManager() {
    }

    public static boolean isVlinder(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.VLINDER_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isVlinder(player)) {
            return;
        }
        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }
        tag.putBoolean(INITIALIZED, true);
        tag.putInt(MEDICAL_CHARGES, MEDICAL_MAX_CHARGES);
        tag.putLong(MEDICAL_NEXT_RECHARGE, 0L);
        tag.putInt(SMOKE_CHARGES, SMOKE_MAX_CHARGES);
        tag.putLong(SMOKE_NEXT_RECHARGE, 0L);
        tag.putLong(CORE_COOLDOWN_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, VlinderTool.NONE.ordinal());
        tag.putInt(DRONE_MODE, VlinderDroneMode.HEAL.ordinal());
        tag.putInt(LOCKED_TARGET_ID, -1);
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
        if (!isVlinder(player)) {
            return;
        }
        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        recharge(player, now, MEDICAL_CHARGES, MEDICAL_NEXT_RECHARGE, MEDICAL_MAX_CHARGES, MEDICAL_RECHARGE_TICKS);
        recharge(player, now, SMOKE_CHARGES, SMOKE_NEXT_RECHARGE, SMOKE_MAX_CHARGES, SMOKE_RECHARGE_TICKS);
        updateLockedTarget(player);
        tickRescue(player);
        tickDowned(player);
    }

    public static void tickDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        long until = tag.getLong(DOWNED_UNTIL);
        if (until <= 0L) {
            if (player.hasEffect(ModEffects.VLINDER_VITAL_DOWNED.get())) {
                player.removeEffect(ModEffects.VLINDER_VITAL_DOWNED.get());
            }
            return;
        }

        int remaining = (int) Math.min(Integer.MAX_VALUE, until - player.level().getGameTime());
        if (remaining <= 0) {
            expireDowned(player);
            return;
        }

        MobEffectInstance current = player.getEffect(ModEffects.VLINDER_VITAL_DOWNED.get());
        if (current == null || current.getDuration() < remaining - 5) {
            player.addEffect(new MobEffectInstance(ModEffects.VLINDER_VITAL_DOWNED.get(),
                    remaining, 0, false, true, true));
        }
        if (player.getHealth() < 1.0F) {
            player.setHealth(1.0F);
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
                && rescueProtectionRemainingTicks(player) <= 0
                && hasVlinderSuppliedBuff(player);
    }

    public static void enterDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        long until = player.level().getGameTime() + DOWNED_DURATION_TICKS;
        tag.putLong(DOWNED_UNTIL, until);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        player.setHealth(Math.max(1.0F, Math.min(player.getHealth(), player.getMaxHealth())));
        player.clearFire();
        player.addEffect(new MobEffectInstance(ModEffects.VLINDER_VITAL_DOWNED.get(),
                DOWNED_DURATION_TICKS, 0, false, true, true));
        player.level().playSound(null, player.blockPosition(), ModSounds.VLINDER_DOWNED_TRIGGER.get(),
                SoundSource.PLAYERS, 0.9F, 1.0F);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.vlinder.downed"), true);
    }

    public static void clearDowned(ServerPlayer player, boolean fullHeal, boolean quick) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        player.removeEffect(ModEffects.VLINDER_VITAL_DOWNED.get());
        player.removeEffect(ModEffects.VLINDER_PLASMA_INJECTED.get());
        if (fullHeal) {
            player.setHealth(player.getMaxHealth());
            player.clearFire();
            player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 4 * 20, 0, false, true, true));
        }
        grantRescueProtection(player);
        player.level().playSound(null, player.blockPosition(),
                quick ? ModSounds.VLINDER_QUICK_RESCUE.get() : ModSounds.VLINDER_RESCUE_COMPLETE.get(),
                SoundSource.PLAYERS, 0.9F, quick ? 1.16F : 1.0F);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.vlinder.revived"), true);
    }

    public static void clearDownedOnDeath(ServerPlayer player) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        tag.putInt(RESCUE_TARGET_ID, -1);
        tag.putInt(RESCUE_TICKS, 0);
        player.removeEffect(ModEffects.VLINDER_VITAL_DOWNED.get());
        player.removeEffect(ModEffects.VLINDER_PLASMA_INJECTED.get());
    }

    public static void clearRuntimeOnDeath(ServerPlayer player) {
        setEquippedTool(player, VlinderTool.NONE);
        clearDownedOnDeath(player);
        VlinderActiveDefenseDroneEntity.discardFor(player);
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

    public static int rescueProtectionRemainingTicks(Player player) {
        long remaining = data(player).getLong(RESCUE_PROTECTION_UNTIL) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
    }

    public static boolean hasVlinderSuppliedBuff(Player player) {
        return data(player).getLong(VLINDER_BUFF_UNTIL) > player.level().getGameTime();
    }

    public static void markVlinderSuppliedBuff(Player player, int durationTicks) {
        long until = player.level().getGameTime() + Math.max(1, durationTicks);
        data(player).putLong(VLINDER_BUFF_UNTIL, Math.max(data(player).getLong(VLINDER_BUFF_UNTIL), until));
    }

    public static void applyHealingDust(ServerPlayer owner, LivingEntity target) {
        target.addEffect(new MobEffectInstance(ModEffects.VLINDER_HEALING_DUST.get(),
                HEALING_DUST_TICKS, 0, false, true, true), owner);
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION,
                HEALING_DUST_TICKS, 2, false, true, true), owner);
        for (MobEffectInstance effect : new ArrayList<>(target.getActiveEffects())) {
            if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                target.removeEffect(effect.getEffect());
            }
        }
        if (target instanceof Player player) {
            markVlinderSuppliedBuff(player, HEALING_DUST_TICKS);
        }
    }

    public static void applyMedicalWaste(ServerPlayer owner, LivingEntity target) {
        target.addEffect(new MobEffectInstance(ModEffects.VLINDER_MEDICAL_WASTE_INTERFERENCE.get(),
                MEDICAL_WASTE_TICKS, 0, false, true, true), owner);
    }

    public static boolean hasMedicalWasteInterference(LivingEntity target) {
        return target.hasEffect(ModEffects.VLINDER_MEDICAL_WASTE_INTERFERENCE.get());
    }

    public static void markPlasmaInjected(ServerPlayer target) {
        int duration = Math.max(20, downedRemainingTicks(target));
        target.addEffect(new MobEffectInstance(ModEffects.VLINDER_PLASMA_INJECTED.get(),
                duration, 0, false, true, true));
    }

    public static VlinderTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        VlinderTool[] tools = VlinderTool.values();
        return ordinal >= 0 && ordinal < tools.length ? tools[ordinal] : VlinderTool.NONE;
    }

    public static void setEquippedTool(Player player, VlinderTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
        if (tool == VlinderTool.NONE) {
            data(player).putInt(LOCKED_TARGET_ID, -1);
        }
    }

    public static VlinderDroneMode droneMode(Player player) {
        int ordinal = data(player).getInt(DRONE_MODE);
        VlinderDroneMode[] modes = VlinderDroneMode.values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : VlinderDroneMode.HEAL;
    }

    public static void toggleDroneMode(Player player) {
        VlinderDroneMode next = droneMode(player) == VlinderDroneMode.HEAL
                ? VlinderDroneMode.INTERFERE
                : VlinderDroneMode.HEAL;
        data(player).putInt(DRONE_MODE, next.ordinal());
    }

    public static int lockedTargetId(Player player) {
        return data(player).getInt(LOCKED_TARGET_ID);
    }

    public static ServerPlayer lockedTarget(ServerPlayer player) {
        int id = lockedTargetId(player);
        if (id < 0) {
            return null;
        }
        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            if (target.getId() == id && target.level() == player.level() && TargetingUtil.isTargetablePlayer(target)) {
                return target;
            }
        }
        return null;
    }

    public static int medicalCharges(Player player) {
        return data(player).getInt(MEDICAL_CHARGES);
    }

    public static int smokeCharges(Player player) {
        return data(player).getInt(SMOKE_CHARGES);
    }

    public static int medicalRechargeRemainingTicks(Player player) {
        return medicalCharges(player) >= MEDICAL_MAX_CHARGES ? 0 : remainingTicks(player, MEDICAL_NEXT_RECHARGE);
    }

    public static int smokeRechargeRemainingTicks(Player player) {
        return smokeCharges(player) >= SMOKE_MAX_CHARGES ? 0 : remainingTicks(player, SMOKE_NEXT_RECHARGE);
    }

    public static int coreCooldownRemainingTicks(Player player) {
        return remainingTicks(player, CORE_COOLDOWN_UNTIL);
    }

    public static boolean consumeMedicalCharge(ServerPlayer player) {
        return consumeCharge(player, MEDICAL_CHARGES, MEDICAL_NEXT_RECHARGE, MEDICAL_MAX_CHARGES, MEDICAL_RECHARGE_TICKS);
    }

    public static boolean consumeSmokeCharge(ServerPlayer player) {
        return consumeCharge(player, SMOKE_CHARGES, SMOKE_NEXT_RECHARGE, SMOKE_MAX_CHARGES, SMOKE_RECHARGE_TICKS);
    }

    public static boolean consumeCore(ServerPlayer player) {
        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        if (now < tag.getLong(CORE_COOLDOWN_UNTIL)) {
            return false;
        }
        tag.putLong(CORE_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, CORE_COOLDOWN_TICKS));
        return true;
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

    public static void syncToClient(ServerPlayer player) {
        if (!isVlinder(player)) {
            return;
        }
        initializeIfNeeded(player);
        VlinderActiveDefenseDroneEntity activeDefense = VlinderActiveDefenseDroneEntity.activeFor(player);
        NetworkHandler.sendToPlayer(new S2C_SyncVlinderState(
                medicalCharges(player),
                MEDICAL_MAX_CHARGES,
                medicalRechargeRemainingTicks(player),
                smokeCharges(player),
                SMOKE_MAX_CHARGES,
                smokeRechargeRemainingTicks(player),
                coreCooldownRemainingTicks(player),
                equippedTool(player).ordinal(),
                droneMode(player).ordinal(),
                lockedTargetId(player),
                rescueTicks(player),
                rescueRequiredTicks(player),
                downedRemainingTicks(player),
                rescueProtectionRemainingTicks(player),
                activeDefense == null ? 0 : activeDefense.remainingTicks(),
                activeDefense == null ? 0 : activeDefense.injectionTicks(),
                activeDefense == null ? 0 : VlinderActiveDefenseDroneEntity.INJECTION_TICKS,
                markers(player, activeDefense)
        ), player);
    }

    private static void updateLockedTarget(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (equippedTool(player) != VlinderTool.MEDICAL_DRONE) {
            tag.putInt(LOCKED_TARGET_ID, -1);
            return;
        }
        int previous = tag.getInt(LOCKED_TARGET_ID);
        ServerPlayer target = findLookTargetPlayer(player);
        int next = target == null ? -1 : target.getId();
        tag.putInt(LOCKED_TARGET_ID, next);
        if (next >= 0 && next != previous) {
            player.level().playSound(null, player.blockPosition(), ModSounds.VLINDER_MEDICAL_DRONE_LOCK.get(),
                    SoundSource.PLAYERS, 0.55F, 1.15F);
        }
    }

    private static ServerPlayer findLookTargetPlayer(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double rangeSqr = LOCK_RANGE * LOCK_RANGE;
        return player.server.getPlayerList().getPlayers().stream()
                .filter(target -> target != player && target.level() == player.level())
                .filter(TargetingUtil::isTargetablePlayer)
                .filter(target -> target.distanceToSqr(player) <= rangeSqr)
                .filter(target -> inLockCone(eye, look, target))
                .min(Comparator.comparingDouble(player::distanceToSqr))
                .orElse(null);
    }

    private static boolean inLockCone(Vec3 eye, Vec3 look, LivingEntity target) {
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
        Vec3 toTarget = targetCenter.subtract(eye);
        double distance = toTarget.length();
        if (distance < 0.001D) {
            return true;
        }
        Vec3 direction = toTarget.scale(1.0D / distance);
        double alignment = look.dot(direction);
        if (alignment < 0.80D) {
            return false;
        }
        double offAxis = Math.sqrt(Math.max(0.0D, 1.0D - alignment * alignment)) * distance;
        return alignment >= 0.975D || offAxis <= 2.6D;
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
            if (ticks == 1) {
                player.level().playSound(null, player.blockPosition(), ModSounds.VLINDER_SELF_RESCUE_START.get(),
                        SoundSource.PLAYERS, 0.8F, 1.0F);
            }
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vlinder.self_rescue",
                    progressBar(ticks, REVIVE_SELF_TICKS)), true);
            if (ticks >= REVIVE_SELF_TICKS) {
                player.level().playSound(null, player.blockPosition(), ModSounds.VLINDER_SELF_RESCUE_COMPLETE.get(),
                        SoundSource.PLAYERS, 0.9F, 1.05F);
                clearDowned(player, true, false);
            }
            return;
        }

        ServerPlayer target = nearestDownedPlayer(player);
        if (target == null) {
            resetRescue(tag);
            return;
        }

        if (target.hasEffect(ModEffects.VLINDER_PLASMA_INJECTED.get())) {
            clearDowned(target, true, true);
            resetRescue(tag);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vlinder.quick_rescue_done",
                    target.getDisplayName()), true);
            return;
        }

        int targetId = target.getId();
        int ticks = tag.getInt(RESCUE_TARGET_ID) == targetId ? tag.getInt(RESCUE_TICKS) + 1 : 1;
        tag.putInt(RESCUE_TARGET_ID, targetId);
        tag.putInt(RESCUE_TICKS, ticks);
        if (ticks == 1) {
            player.level().playSound(null, player.blockPosition(), ModSounds.VLINDER_RESCUE_START.get(),
                    SoundSource.PLAYERS, 0.75F, 1.0F);
        }
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.vlinder.rescuing",
                target.getDisplayName(), progressBar(ticks, REVIVE_OTHER_TICKS)), true);
        if (ticks >= REVIVE_OTHER_TICKS) {
            clearDowned(target, true, false);
            resetRescue(tag);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vlinder.rescue_done",
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

    private static List<VlinderWorldMarker> markers(ServerPlayer player, VlinderActiveDefenseDroneEntity activeDefense) {
        List<VlinderWorldMarker> markers = new ArrayList<>();
        boolean showPlayers = equippedTool(player) == VlinderTool.MEDICAL_DRONE;
        int lockedId = lockedTargetId(player);
        for (ServerPlayer target : player.server.getPlayerList().getPlayers()) {
            if (target.level() != player.level() || !TargetingUtil.isTargetablePlayer(target)) {
                continue;
            }
            if (showPlayers && target != player) {
                markers.add(new VlinderWorldMarker(
                        target.getId() == lockedId ? VlinderMarkerType.LOCKED_PLAYER : VlinderMarkerType.PLAYER,
                        target.getId(),
                        target.position(),
                        10,
                        0,
                        0
                ));
            }
            if (isDowned(target)) {
                markers.add(new VlinderWorldMarker(
                        VlinderMarkerType.DOWNED_PLAYER,
                        target.getId(),
                        target.position(),
                        downedRemainingTicks(target),
                        0,
                        0
                ));
            }
        }
        if (activeDefense != null && activeDefense.injectionTargetId() >= 0) {
            markers.add(new VlinderWorldMarker(
                    VlinderMarkerType.PLASMA_INJECTION,
                    activeDefense.injectionTargetId(),
                    activeDefense.injectionPosition(),
                    activeDefense.remainingTicks(),
                    activeDefense.injectionTicks(),
                    VlinderActiveDefenseDroneEntity.INJECTION_TICKS
            ));
        }
        markers.sort(Comparator.comparingDouble(marker -> marker.position().distanceToSqr(player.position())));
        return markers.size() > 36 ? List.copyOf(markers.subList(0, 36)) : List.copyOf(markers);
    }

    private static void grantRescueProtection(ServerPlayer player) {
        long until = player.level().getGameTime() + RESCUE_PROTECTION_TICKS;
        data(player).putLong(RESCUE_PROTECTION_UNTIL, until);
        player.addEffect(new MobEffectInstance(ModEffects.VLINDER_RESCUE_PROTECTION.get(),
                RESCUE_PROTECTION_TICKS, 0, false, true, true));
    }

    private static void expireDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putBoolean(EXECUTING_DOWNED_DEATH, true);
        player.removeEffect(ModEffects.VLINDER_VITAL_DOWNED.get());
        player.removeEffect(ModEffects.VLINDER_PLASMA_INJECTED.get());
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

    private static boolean consumeCharge(ServerPlayer player, String chargeKey, String rechargeKey, int max, int rechargeTicks) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(chargeKey);
        if (charges <= 0) {
            return false;
        }
        tag.putInt(chargeKey, charges - 1);
        if (charges - 1 < max && tag.getLong(rechargeKey) <= 0L) {
            tag.putLong(rechargeKey, SkillCooldownHelper.until(player, player.level().getGameTime(), rechargeTicks));
        }
        return true;
    }

    private static void recharge(ServerPlayer player, long now, String chargeKey, String rechargeKey, int max, int rechargeTicks) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(chargeKey);
        if (charges >= max) {
            tag.putLong(rechargeKey, 0L);
            return;
        }
        long next = tag.getLong(rechargeKey);
        if (next <= 0L) {
            tag.putLong(rechargeKey, SkillCooldownHelper.until(player, now, rechargeTicks));
            return;
        }
        while (charges < max && now >= next) {
            charges++;
            next += SkillCooldownHelper.ticks(player, rechargeTicks);
        }
        tag.putInt(chargeKey, charges);
        tag.putLong(rechargeKey, charges >= max ? 0L : next);
    }

    private static int remainingTicks(Player player, String key) {
        long remaining = data(player).getLong(key) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
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

    private static CompoundTag data(Player player) {
        CompoundTag persistent = player.getPersistentData();
        if (!persistent.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            persistent.put(ROOT_TAG, new CompoundTag());
        }
        return persistent.getCompound(ROOT_TAG);
    }
}
