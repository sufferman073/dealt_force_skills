package com.rzy.dealt_force_skills.character.toxik;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncToxikState;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public final class ToxikStateManager {
    public static final int ADRENALINE_COOLDOWN_TICKS = 45 * 20;
    public static final int ADRENALINE_BASE_DURATION_TICKS = 5 * 20;
    public static final int TEAR_GAS_MAX_CHARGES = 2;
    public static final int TEAR_GAS_RECHARGE_TICKS = 35 * 20;
    public static final int FIREFLY_COOLDOWN_TICKS = 90 * 20;
    public static final int FIREFLY_BASE_DURATION_TICKS = 10 * 20;
    public static final int FIREFLY_PULLOUT_TICKS = 4 * 20;
    public static final double ADRENALINE_RADIUS = 5.0D;
    public static final double FIREFLY_RANGE = 30.0D;
    private static final double PASSIVE_BEHAVIOR_MULTIPLIER = 11.0D;
    private static final double FIREFLY_MIN_ALIGNMENT = 0.18D;
    private static final double FIREFLY_MAX_OFF_AXIS = 18.0D;
    private static final int FIREFLY_FAN_HORIZONTAL_SAMPLES = 13;
    private static final int FIREFLY_FAN_VERTICAL_SAMPLES = 5;
    private static final DustParticleOptions FIREFLY_DUST = new DustParticleOptions(new Vector3f(0.72f, 1.0f, 0.22f), 1.15f);
    private static final Set<java.util.UUID> EFFECT_SCALING_GUARD = new HashSet<>();

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".toxik";
    private static final String INITIALIZED = "Initialized";
    private static final String ADRENALINE_COOLDOWN_UNTIL = "AdrenalineCooldownUntil";
    private static final String TEAR_GAS_CHARGES = "TearGasCharges";
    private static final String TEAR_GAS_NEXT_RECHARGE = "TearGasNextRecharge";
    private static final String FIREFLY_COOLDOWN_UNTIL = "FireflyCooldownUntil";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String FIREFLY_MODE = "FireflyMode";
    private static final String FIREFLY_PULLOUT_ACTIVE = "FireflyPulloutActive";
    private static final String FIREFLY_PULLOUT_TICKS_KEY = "FireflyPulloutTicks";

    private ToxikStateManager() {
    }

    public static boolean isToxik(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.TOXIK_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isToxik(player)) {
            return;
        }
        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }
        tag.putBoolean(INITIALIZED, true);
        tag.putLong(ADRENALINE_COOLDOWN_UNTIL, 0L);
        tag.putInt(TEAR_GAS_CHARGES, TEAR_GAS_MAX_CHARGES);
        tag.putLong(TEAR_GAS_NEXT_RECHARGE, 0L);
        tag.putLong(FIREFLY_COOLDOWN_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, ToxikTool.NONE.ordinal());
        tag.putInt(FIREFLY_MODE, ToxikFireflyMode.LETHAL.ordinal());
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
        if (!isToxik(player)) {
            return;
        }
        initializeIfNeeded(player);
        rechargeTearGas(player, player.level().getGameTime());
    }

    public static void tickFireflyPullout(ServerPlayer player) {
        CompoundTag tag = data(player);
        boolean active = tag.getBoolean(FIREFLY_PULLOUT_ACTIVE);
        if (!player.hasEffect(ModEffects.TOXIK_FIREFLY_INTERFERENCE.get())) {
            tag.putBoolean(FIREFLY_PULLOUT_ACTIVE, false);
            tag.putInt(FIREFLY_PULLOUT_TICKS_KEY, 0);
            return;
        }
        if (!active) {
            tag.putInt(FIREFLY_PULLOUT_TICKS_KEY, 0);
            return;
        }

        int ticks = tag.getInt(FIREFLY_PULLOUT_TICKS_KEY) + 1;
        tag.putInt(FIREFLY_PULLOUT_TICKS_KEY, ticks);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.toxik.firefly_pullout",
                progressBar(ticks, FIREFLY_PULLOUT_TICKS)), true);
        if (ticks >= FIREFLY_PULLOUT_TICKS) {
            player.removeEffect(ModEffects.TOXIK_FIREFLY_INTERFERENCE.get());
            tag.putBoolean(FIREFLY_PULLOUT_ACTIVE, false);
            tag.putInt(FIREFLY_PULLOUT_TICKS_KEY, 0);
            player.level().playSound(null, player.blockPosition(), ModSounds.TOXIK_FIREFLY_PULLOUT_COMPLETE.get(),
                    SoundSource.PLAYERS, 0.8f, 1.05f);
        }
    }

    public static void setFireflyPulloutActive(ServerPlayer player, boolean active) {
        CompoundTag tag = data(player);
        if (!player.hasEffect(ModEffects.TOXIK_FIREFLY_INTERFERENCE.get())) {
            tag.putBoolean(FIREFLY_PULLOUT_ACTIVE, false);
            tag.putInt(FIREFLY_PULLOUT_TICKS_KEY, 0);
            return;
        }
        if (active && !tag.getBoolean(FIREFLY_PULLOUT_ACTIVE)) {
            player.level().playSound(null, player.blockPosition(), ModSounds.TOXIK_FIREFLY_PULLOUT_START.get(),
                    SoundSource.PLAYERS, 0.55f, 1.0f);
        }
        tag.putBoolean(FIREFLY_PULLOUT_ACTIVE, active);
        if (!active) {
            tag.putInt(FIREFLY_PULLOUT_TICKS_KEY, 0);
        }
    }

    public static double behaviorSpeedMultiplier(LivingEntity entity) {
        double multiplier = 1.0D;
        if (entity instanceof Player player && isToxik(player)) {
            multiplier *= PASSIVE_BEHAVIOR_MULTIPLIER;
        }
        MobEffectInstance adrenaline = entity.getEffect(ModEffects.TOXIK_ADRENALINE.get());
        if (adrenaline != null) {
            multiplier *= adrenalineMultiplier(adrenaline.getAmplifier());
        }
        return multiplier;
    }

    public static double adrenalineMultiplier(int amplifier) {
        return 1.0D + 0.2D * (amplifier + 1);
    }

    public static double adrenalineSpeedMultiplier(LivingEntity entity) {
        MobEffectInstance adrenaline = entity.getEffect(ModEffects.TOXIK_ADRENALINE.get());
        return adrenaline == null ? 1.0D : adrenalineMultiplier(adrenaline.getAmplifier());
    }

    public static void accelerateCooldowns(Player player) {
        MobEffectInstance adrenaline = player.getEffect(ModEffects.TOXIK_ADRENALINE.get());
        if (adrenaline == null) {
            return;
        }
        int extraTicks = Math.max(0, (int) Math.floor(adrenalineMultiplier(adrenaline.getAmplifier()) - 1.0D));
        for (int i = 0; i < extraTicks; i++) {
            player.getCooldowns().tick();
        }
    }

    public static boolean useAdrenaline(ServerPlayer player) {
        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        if (now < data(player).getLong(ADRENALINE_COOLDOWN_UNTIL)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.toxik.adrenaline_cooldown"), true);
            return true;
        }
        data(player).putLong(ADRENALINE_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, now, ADRENALINE_COOLDOWN_TICKS));
        AABB box = player.getBoundingBox().inflate(ADRENALINE_RADIUS);
        for (Player target : player.level().getEntitiesOfClass(Player.class, box,
                target -> (target == player || TargetingUtil.isTargetablePlayer(target))
                        && target.distanceTo(player) <= ADRENALINE_RADIUS)) {
            applyAdrenaline(player, target, ADRENALINE_BASE_DURATION_TICKS);
        }
        player.level().playSound(null, player.blockPosition(), ModSounds.TOXIK_ADRENALINE_RELEASE.get(),
                SoundSource.PLAYERS, 0.95f, 1.0f);
        return true;
    }

    public static void applyAdrenaline(ServerPlayer owner, LivingEntity target, int baseTicks) {
        MobEffectInstance baseEffect = new MobEffectInstance(ModEffects.TOXIK_ADRENALINE.get(),
                baseTicks, 0, false, true, true);
        for (MobEffectInstance active : new ArrayList<>(target.getActiveEffects())) {
            if (active.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                target.removeEffect(active.getEffect());
            }
        }
        addToxikScaledEffect(owner, target, baseEffect);
    }

    public static boolean addToxikScaledEffect(ServerPlayer owner, LivingEntity target, MobEffect effect,
                                               int baseDurationTicks, int baseAmplifier) {
        return addToxikScaledEffect(owner, target,
                new MobEffectInstance(effect, baseDurationTicks, baseAmplifier, false, true, true));
    }

    public static boolean addToxikScaledEffect(ServerPlayer owner, LivingEntity target, MobEffectInstance baseEffect) {
        MobEffectInstance scaled = scaledEffect(owner, target, baseEffect);
        return addEffectWithScalingGuard(owner, target, scaled);
    }

    public static MobEffectInstance scaledEffect(
            ServerPlayer owner,
            LivingEntity target,
            MobEffect effect,
            int baseDurationTicks,
            int baseAmplifier,
            boolean positive
    ) {
        return scaledEffect(owner, target,
                new MobEffectInstance(effect, baseDurationTicks, baseAmplifier, false, true, true), positive);
    }

    public static MobEffectInstance scaledEffect(ServerPlayer owner, LivingEntity target, MobEffectInstance original) {
        boolean positive = original.getEffect().getCategory() == MobEffectCategory.BENEFICIAL;
        return scaledEffect(owner, target, original, positive);
    }

    private static MobEffectInstance scaledEffect(ServerPlayer owner, LivingEntity target,
                                                  MobEffectInstance original, boolean positive) {
        int duration = Math.max(1, original.getDuration());
        int amplifier = Math.max(0, original.getAmplifier());
        if (owner != null && isToxik(owner)) {
            boolean self = target.getUUID().equals(owner.getUUID());
            if (self && positive) {
                duration = scaleDuration(duration, 20);
                amplifier = scaleAmplifier(amplifier, 20);
            } else if (self) {
                duration = Math.max(1, duration / 100);
                amplifier = 0;
            } else if (target instanceof Player) {
                duration = scaleDuration(duration, 5);
                amplifier = scaleAmplifier(amplifier, 5);
            } else if (!positive) {
                duration = scaleDuration(duration, 20);
                amplifier = scaleAmplifier(amplifier, 20);
            }
        }
        return new MobEffectInstance(original.getEffect(), duration, amplifier,
                original.isAmbient(), original.isVisible(), original.showIcon());
    }

    public static boolean tryRescaleAddedEffect(LivingEntity target, MobEffectInstance added, Entity effectSource) {
        if (target.level().isClientSide || added == null || isScalingMobEffect(target) || shouldSkipAutoScale(added)) {
            return false;
        }

        ServerPlayer owner = null;
        if (effectSource instanceof ServerPlayer sourcePlayer && isToxik(sourcePlayer)) {
            owner = sourcePlayer;
        } else if (effectSource == null
                && target instanceof ServerPlayer selfPlayer
                && isToxik(selfPlayer)
                && added.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
            // Vanilla self-applied effects often arrive without an effect source. Treat Toxik's own
            // beneficial potion/mod buffs as self-applied so the passive also works outside custom skills.
            owner = selfPlayer;
        }

        if (owner == null) {
            return false;
        }

        MobEffectInstance scaled = scaledEffect(owner, target, added);
        if (scaled.getDuration() == added.getDuration() && scaled.getAmplifier() == added.getAmplifier()) {
            return false;
        }
        return addEffectWithScalingGuard(owner, target, scaled);
    }

    public static boolean isScalingMobEffect(LivingEntity target) {
        return EFFECT_SCALING_GUARD.contains(target.getUUID());
    }

    private static boolean addEffectWithScalingGuard(ServerPlayer owner, LivingEntity target, MobEffectInstance effect) {
        java.util.UUID id = target.getUUID();
        EFFECT_SCALING_GUARD.add(id);
        try {
            return target.addEffect(effect, owner);
        } finally {
            EFFECT_SCALING_GUARD.remove(id);
        }
    }

    private static boolean shouldSkipAutoScale(MobEffectInstance effect) {
        return effect.getEffect() == ModEffects.CHARACTER_FRAMEWORK.get();
    }

    public static boolean consumeTearGas(ServerPlayer player) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(TEAR_GAS_CHARGES);
        if (charges <= 0) {
            return false;
        }
        tag.putInt(TEAR_GAS_CHARGES, charges - 1);
        if (charges - 1 < TEAR_GAS_MAX_CHARGES && tag.getLong(TEAR_GAS_NEXT_RECHARGE) <= 0L) {
            tag.putLong(TEAR_GAS_NEXT_RECHARGE,
                    SkillCooldownHelper.until(player, player.level().getGameTime(), TEAR_GAS_RECHARGE_TICKS));
        }
        return true;
    }

    public static boolean consumeFirefly(ServerPlayer player) {
        long now = player.level().getGameTime();
        if (now < data(player).getLong(FIREFLY_COOLDOWN_UNTIL)) {
            return false;
        }
        data(player).putLong(FIREFLY_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, now, FIREFLY_COOLDOWN_TICKS));
        return true;
    }

    public static ToxikTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        ToxikTool[] tools = ToxikTool.values();
        return ordinal >= 0 && ordinal < tools.length ? tools[ordinal] : ToxikTool.NONE;
    }

    public static void setEquippedTool(Player player, ToxikTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
    }

    public static ToxikFireflyMode fireflyMode(Player player) {
        int ordinal = data(player).getInt(FIREFLY_MODE);
        ToxikFireflyMode[] modes = ToxikFireflyMode.values();
        return ordinal >= 0 && ordinal < modes.length ? modes[ordinal] : ToxikFireflyMode.LETHAL;
    }

    public static void toggleFireflyMode(Player player) {
        ToxikFireflyMode next = fireflyMode(player) == ToxikFireflyMode.LETHAL
                ? ToxikFireflyMode.AMPLIFY
                : ToxikFireflyMode.LETHAL;
        data(player).putInt(FIREFLY_MODE, next.ordinal());
    }

    public static void releaseFirefly(ServerPlayer player) {
        if (!consumeFirefly(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.toxik.firefly_cooldown"), true);
            return;
        }
        ToxikFireflyMode mode = fireflyMode(player);
        ServerLevel level = player.serverLevel();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        AABB box = player.getBoundingBox().inflate(FIREFLY_RANGE);
        int hitCount = 0;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (target == player || !TargetingUtil.isTargetableLiving(target) || !isInFireflyCone(level, eye, look, target)) {
                continue;
            }
            if (mode == ToxikFireflyMode.AMPLIFY && target instanceof Player) {
                applyAdrenaline(player, target, FIREFLY_BASE_DURATION_TICKS);
            } else {
                applyFireflyInterference(player, target, FIREFLY_BASE_DURATION_TICKS);
            }
            hitCount++;
        }

        spawnFireflyParticles(level, eye, look);
        level.playSound(null, player.blockPosition(), ModSounds.TOXIK_FIREFLY_RELEASE.get(),
                SoundSource.PLAYERS, 0.95f, mode == ToxikFireflyMode.LETHAL ? 0.95f : 1.12f);
        if (hitCount > 0) {
            level.playSound(null, player.blockPosition(), ModSounds.TOXIK_FIREFLY_HIT.get(),
                    SoundSource.PLAYERS, 0.75f, 1.0f);
        }
        setEquippedTool(player, ToxikTool.NONE);
    }

    public static void applyFireflyInterference(ServerPlayer owner, LivingEntity target, int baseTicks) {
        addToxikScaledEffect(owner, target, ModEffects.TOXIK_FIREFLY_INTERFERENCE.get(), baseTicks, 0);
        com.rzy.dealt_force_skills.effect.ToxikFireflyInterferenceEffect.setOwner(target, owner);
    }

    public static int adrenalineCooldownRemainingTicks(Player player) {
        return remainingTicks(player, ADRENALINE_COOLDOWN_UNTIL);
    }

    public static int tearGasCharges(Player player) {
        return data(player).getInt(TEAR_GAS_CHARGES);
    }

    public static int tearGasRechargeRemainingTicks(Player player) {
        if (tearGasCharges(player) >= TEAR_GAS_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, TEAR_GAS_NEXT_RECHARGE);
    }

    public static int fireflyCooldownRemainingTicks(Player player) {
        return remainingTicks(player, FIREFLY_COOLDOWN_UNTIL);
    }

    public static void clearRuntimeOnDeath(ServerPlayer player) {
        data(player).putBoolean(FIREFLY_PULLOUT_ACTIVE, false);
        data(player).putInt(FIREFLY_PULLOUT_TICKS_KEY, 0);
        setEquippedTool(player, ToxikTool.NONE);
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isToxik(player)) {
            return;
        }
        initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_SyncToxikState(
                adrenalineCooldownRemainingTicks(player),
                tearGasCharges(player),
                TEAR_GAS_MAX_CHARGES,
                tearGasRechargeRemainingTicks(player),
                fireflyCooldownRemainingTicks(player),
                equippedTool(player).ordinal(),
                fireflyMode(player).ordinal(),
                data(player).getInt(FIREFLY_PULLOUT_TICKS_KEY),
                FIREFLY_PULLOUT_TICKS
        ), player);
    }

    private static boolean isInFireflyCone(ServerLevel level, Vec3 eye, Vec3 look, LivingEntity target) {
        boolean inFan = false;
        double nearestDistance = Double.MAX_VALUE;
        for (Vec3 sample : fireflySamplePoints(target)) {
            Vec3 toSample = sample.subtract(eye);
            double distance = toSample.length();
            if (!isFireflySampleInFan(eye, look, sample, distance)) {
                continue;
            }
            inFan = true;
            nearestDistance = Math.min(nearestDistance, distance);
            if (hasDirectFireflyLine(level, eye, sample, target)) {
                return true;
            }
        }
        return inFan && hasFireflyPathThroughOpenFan(level, eye, look, target, nearestDistance);
    }

    private static boolean isFireflySampleInFan(Vec3 eye, Vec3 look, Vec3 sample, double distance) {
        if (distance > FIREFLY_RANGE || distance < 0.001D) {
            return false;
        }
        Vec3 direction = sample.subtract(eye).scale(1.0D / distance);
        double alignment = look.dot(direction);
        if (alignment < FIREFLY_MIN_ALIGNMENT) {
            return false;
        }
        double offAxis = Math.sqrt(Math.max(0.0D, 1.0D - alignment * alignment)) * distance;
        return offAxis <= FIREFLY_MAX_OFF_AXIS;
    }

    private static boolean hasDirectFireflyLine(ServerLevel level, Vec3 eye, Vec3 sample, LivingEntity target) {
        HitResult result = level.clip(new ClipContext(eye, sample, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(sample) < 0.6D;
    }

    private static boolean hasFireflyPathThroughOpenFan(ServerLevel level, Vec3 eye, Vec3 look,
                                                        LivingEntity target, double targetDistance) {
        if (targetDistance > FIREFLY_RANGE || targetDistance < 0.001D) {
            return false;
        }
        Vec3 direction = look.lengthSqr() < 0.0001D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
        Vec3 right = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 0.0001D) {
            right = new Vec3(1.0D, 0.0D, 0.0D);
        }
        right = right.normalize();
        Vec3 up = right.cross(direction).normalize();
        double horizontalRadius = Math.min(FIREFLY_MAX_OFF_AXIS, Math.max(0.75D, targetDistance * 0.62D));
        double verticalRadius = Math.min(4.5D, horizontalRadius * 0.45D);
        double rayDistance = Math.min(FIREFLY_RANGE, targetDistance + 0.55D);
        double requiredOpenDistance = Math.max(0.0D, targetDistance - 0.45D);

        for (int h = 0; h < FIREFLY_FAN_HORIZONTAL_SAMPLES; h++) {
            double horizontal = sampleOffset(h, FIREFLY_FAN_HORIZONTAL_SAMPLES, horizontalRadius);
            for (int v = 0; v < FIREFLY_FAN_VERTICAL_SAMPLES; v++) {
                double vertical = sampleOffset(v, FIREFLY_FAN_VERTICAL_SAMPLES, verticalRadius);
                Vec3 fanPoint = eye.add(direction.scale(targetDistance))
                        .add(right.scale(horizontal))
                        .add(up.scale(vertical));
                Vec3 ray = fanPoint.subtract(eye);
                if (ray.lengthSqr() < 0.0001D) {
                    continue;
                }
                Vec3 end = eye.add(ray.normalize().scale(rayDistance));
                HitResult result = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, target));
                if (result.getType() == HitResult.Type.MISS || result.getLocation().distanceTo(eye) >= requiredOpenDistance) {
                    return true;
                }
            }
        }
        return false;
    }

    private static double sampleOffset(int index, int samples, double radius) {
        if (samples <= 1) {
            return 0.0D;
        }
        return (((double) index / (double) (samples - 1)) * 2.0D - 1.0D) * radius;
    }

    private static Vec3[] fireflySamplePoints(LivingEntity target) {
        double x = target.getX();
        double y = target.getY();
        double z = target.getZ();
        double height = target.getBbHeight();
        double radius = Math.max(0.35D, target.getBbWidth() * 0.55D);
        return new Vec3[]{
                target.getEyePosition(),
                new Vec3(x, y + height * 0.70D, z),
                new Vec3(x, y + height * 0.45D, z),
                new Vec3(x + radius, y + height * 0.55D, z),
                new Vec3(x - radius, y + height * 0.55D, z),
                new Vec3(x, y + height * 0.55D, z + radius),
                new Vec3(x, y + height * 0.55D, z - radius)
        };
    }

    private static void spawnFireflyParticles(ServerLevel level, Vec3 eye, Vec3 look) {
        Vec3 direction = look.lengthSqr() < 0.0001D ? new Vec3(0.0D, 0.0D, 1.0D) : look.normalize();
        Vec3 side = direction.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (side.lengthSqr() < 0.0001D) {
            side = new Vec3(1.0D, 0.0D, 0.0D);
        }
        side = side.normalize();
        Vec3 up = side.cross(direction).normalize();
        for (int i = 1; i <= 24; i++) {
            double distance = i * (FIREFLY_RANGE / 24.0D);
            double spread = Math.min(FIREFLY_MAX_OFF_AXIS, 0.6D + distance * 0.46D);
            Vec3 center = eye.add(direction.scale(distance));
            level.sendParticles(FIREFLY_DUST,
                    center.x,
                    center.y,
                    center.z,
                    8,
                    spread * 0.45D,
                    spread * 0.22D,
                    spread * 0.45D,
                    0.018D);
            Vec3 leftStream = center.add(side.scale(spread * 0.55D)).add(up.scale(Math.sin(distance) * 0.22D));
            Vec3 rightStream = center.add(side.scale(-spread * 0.55D)).add(up.scale(Math.cos(distance) * 0.22D));
            level.sendParticles(FIREFLY_DUST, leftStream.x, leftStream.y, leftStream.z, 3, 0.08D, 0.08D, 0.08D, 0.035D);
            level.sendParticles(FIREFLY_DUST, rightStream.x, rightStream.y, rightStream.z, 3, 0.08D, 0.08D, 0.08D, 0.035D);
        }
    }

    private static void rechargeTearGas(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(TEAR_GAS_CHARGES);
        if (charges >= TEAR_GAS_MAX_CHARGES) {
            tag.putLong(TEAR_GAS_NEXT_RECHARGE, 0L);
            return;
        }
        long next = tag.getLong(TEAR_GAS_NEXT_RECHARGE);
        if (next <= 0L) {
            tag.putLong(TEAR_GAS_NEXT_RECHARGE,
                    SkillCooldownHelper.until(player, now, TEAR_GAS_RECHARGE_TICKS));
            return;
        }
        while (charges < TEAR_GAS_MAX_CHARGES && now >= next) {
            charges++;
            next += SkillCooldownHelper.ticks(player, TEAR_GAS_RECHARGE_TICKS);
        }
        tag.putInt(TEAR_GAS_CHARGES, charges);
        tag.putLong(TEAR_GAS_NEXT_RECHARGE, charges >= TEAR_GAS_MAX_CHARGES ? 0L : next);
    }

    private static int remainingTicks(Player player, String key) {
        long remaining = data(player).getLong(key) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
    }

    private static int scaleAmplifier(int baseAmplifier, int multiplier) {
        long scaled = ((long) baseAmplifier + 1L) * multiplier - 1L;
        return (int) Math.max(0L, Math.min(Integer.MAX_VALUE, scaled));
    }

    private static int scaleDuration(int baseDuration, int multiplier) {
        long scaled = (long) Math.max(1, baseDuration) * multiplier;
        return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, scaled));
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
