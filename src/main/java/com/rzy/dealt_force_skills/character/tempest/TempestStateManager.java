package com.rzy.dealt_force_skills.character.tempest;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.character.electronics.ElectronicInterferenceManager;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncTempestState;
import com.rzy.dealt_force_skills.network.S2C_TempestStartRoll;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class TempestStateManager {
    public static final int ROLL_COOLDOWN_TICKS = 20 * 20;
    public static final int WALL_MAX_CHARGES = 2;
    public static final int WALL_RECHARGE_TICKS = 35 * 20;
    public static final int CORE_COOLDOWN_TICKS = 110 * 20;
    public static final int EXPLOSIVE_SPINE_TICKS = 2 * 20;
    public static final int DISARMED_TICKS = 3 * 20;
    public static final int DOWNED_SELF_RESCUE_TICKS = 5 * 20;
    public static final double ROPE_MAX_LENGTH = 300.0D;
    public static final double RECALL_SPEED_PER_TICK = 3.0D;

    private static final double PATH_NODE_DISTANCE = 1.0D;
    private static final double NEAR_MISS_RADIUS = 2.0D;
    private static final int ROLL_BOOST_TICKS_TOTAL = 9;
    private static final double ROLL_BOOST_SPEED_PER_TICK = 2.75D;
    private static final DustParticleOptions SPINE_DUST = new DustParticleOptions(new Vector3f(0.48f, 1.0f, 0.36f), 1.1f);
    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".tempest";
    private static final String INITIALIZED = "Initialized";
    private static final String WALL_CHARGES = "WallCharges";
    private static final String WALL_NEXT_RECHARGE = "WallNextRecharge";
    private static final String ROLL_COOLDOWN_UNTIL = "RollCooldownUntil";
    private static final String ROLL_BOOST_TICKS = "RollBoostTicks";
    private static final String ROLL_BOOST_DIR_X = "RollBoostDirX";
    private static final String ROLL_BOOST_DIR_Z = "RollBoostDirZ";
    private static final String PENDING_LANDING_ROLL = "PendingLandingRoll";
    private static final String FALL_PROTECTION_TICKS = "FallProtectionTicks";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String CORE_COOLDOWN_UNTIL = "CoreCooldownUntil";
    private static final String ROPE_ACTIVE = "RopeActive";
    private static final String RECALLING = "Recalling";
    private static final String AUTO_RECALL = "AutoRecall";
    private static final String RECALL_INDEX = "RecallIndex";
    private static final String ROPE_LENGTH = "RopeLength";
    private static final String ROPE_DIMENSION = "RopeDimension";
    private static final String PATH = "Path";
    private static final String ANCHOR = "Anchor";
    private static final String LAST_WARNING_TICK = "LastWarningTick";
    private static final String FALL_START_Y = "FallStartY";
    private static final String SUSPENDED = "Suspended";
    private static final String DOWNED_UNTIL = "DownedUntil";
    private static final String SELF_RESCUE_TICKS = "SelfRescueTicks";

    private static final Map<UUID, Map<Integer, Long>> NEAR_MISS_COOLDOWNS = new HashMap<>();

    private TempestStateManager() {
    }

    public static boolean isTempest(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.TEMPEST_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isTempest(player)) {
            return;
        }
        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }
        tag.putBoolean(INITIALIZED, true);
        tag.putInt(WALL_CHARGES, WALL_MAX_CHARGES);
        tag.putLong(WALL_NEXT_RECHARGE, 0L);
        tag.putLong(ROLL_COOLDOWN_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, TempestTool.NONE.ordinal());
        tag.putLong(CORE_COOLDOWN_UNTIL, 0L);
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
        if (!isTempest(player)) {
            return;
        }
        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        recharge(player, now);
        tickProjectileNearMiss(player, now);
        tickPendingLandingRoll(player);
        tickDowned(player);
        if (isDowned(player)) {
            return;
        }
        tickRollBoost(player);
        if (isRecalling(player)) {
            tickRecall(player);
        } else if (isRopeActive(player)) {
            tickRope(player);
        }
    }

    public static boolean useRoll(ServerPlayer player) {
        initializeIfNeeded(player);
        CompoundTag tag = data(player);
        if (tag.getBoolean(PENDING_LANDING_ROLL)) {
            tag.putBoolean(PENDING_LANDING_ROLL, false);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.roll_canceled"), true);
            return true;
        }
        if (isActionLocked(player) || player.isInWaterOrBubble()) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.action_locked"), true);
            return true;
        }
        long now = player.level().getGameTime();
        if (now < tag.getLong(ROLL_COOLDOWN_UNTIL)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.roll_cooldown"), true);
            return true;
        }
        tag.putLong(ROLL_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, ROLL_COOLDOWN_TICKS));
        player.clearFire();
        if (player.onGround()) {
            startClientRoll(player);
            startRollBoost(player);
            tag.putInt(FALL_PROTECTION_TICKS, 8);
        } else {
            tag.putBoolean(PENDING_LANDING_ROLL, true);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.roll_prepared"), true);
        }
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_TACTICAL_ROLL_START.get(),
                SoundSource.PLAYERS, 0.85F, 1.0F);
        triggerExplosiveSpine(player, ModSounds.TEMPEST_EXPLOSIVE_SPINE_ACTIVATE.get());
        return true;
    }

    public static void resetRollCooldownOnKill(ServerPlayer player) {
        if (!isTempest(player)) {
            return;
        }
        CompoundTag tag = data(player);
        if (rollCooldownRemainingTicks(player) <= 0) {
            return;
        }
        tag.putLong(ROLL_COOLDOWN_UNTIL, 0L);
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_TACTICAL_ROLL_KILL_RESET.get(),
                SoundSource.PLAYERS, 0.8F, 1.05F);
        syncToClient(player);
    }

    public static boolean consumeFallProtection(ServerPlayer player) {
        if (!isTempest(player)) {
            return false;
        }
        CompoundTag tag = data(player);
        if (!tag.getBoolean(PENDING_LANDING_ROLL) && tag.getInt(FALL_PROTECTION_TICKS) <= 0) {
            return false;
        }
        tag.putBoolean(PENDING_LANDING_ROLL, false);
        tag.putInt(FALL_PROTECTION_TICKS, 8);
        player.fallDistance = 0.0F;
        player.clearFire();
        startClientRoll(player);
        startRollBoost(player);
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_TACTICAL_ROLL_START.get(),
                SoundSource.PLAYERS, 0.85F, 1.0F);
        return true;
    }

    public static boolean consumeWallCharge(ServerPlayer player) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(WALL_CHARGES);
        if (charges <= 0) {
            return false;
        }
        tag.putInt(WALL_CHARGES, charges - 1);
        if (charges - 1 < WALL_MAX_CHARGES && tag.getLong(WALL_NEXT_RECHARGE) <= 0L) {
            tag.putLong(WALL_NEXT_RECHARGE,
                    SkillCooldownHelper.until(player, player.level().getGameTime(), WALL_RECHARGE_TICKS));
        }
        return true;
    }

    public static int wallCharges(Player player) {
        return data(player).getInt(WALL_CHARGES);
    }

    public static int wallRechargeRemainingTicks(Player player) {
        return wallCharges(player) >= WALL_MAX_CHARGES ? 0 : remainingTicks(player, WALL_NEXT_RECHARGE);
    }

    public static int rollCooldownRemainingTicks(Player player) {
        return remainingTicks(player, ROLL_COOLDOWN_UNTIL);
    }

    public static int coreCooldownRemainingTicks(Player player) {
        return remainingTicks(player, CORE_COOLDOWN_UNTIL);
    }

    public static TempestTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        TempestTool[] tools = TempestTool.values();
        return ordinal >= 0 && ordinal < tools.length ? tools[ordinal] : TempestTool.NONE;
    }

    public static void setEquippedTool(Player player, TempestTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
    }

    public static boolean isRopeActive(Player player) {
        return data(player).getBoolean(ROPE_ACTIVE);
    }

    public static boolean isRecalling(Player player) {
        return data(player).getBoolean(RECALLING);
    }

    public static boolean isDowned(Player player) {
        return data(player).getLong(DOWNED_UNTIL) > player.level().getGameTime();
    }

    public static boolean isActionLocked(Player player) {
        return isRecalling(player) || isDowned(player) || player.hasEffect(ModEffects.TEMPEST_DISARMED.get());
    }

    public static boolean shouldPreventFatalDamage(ServerPlayer player, float incomingDamage) {
        return incomingDamage >= player.getHealth()
                && isRopeActive(player)
                && !isRecalling(player)
                && !isDowned(player);
    }

    public static void triggerEmergencyRecall(ServerPlayer player) {
        if (!isRopeActive(player)) {
            return;
        }
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_LETHAL_TRIGGER.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
        startRecall(player, true);
    }

    public static boolean useCore(ServerPlayer player) {
        initializeIfNeeded(player);
        if (isActionLocked(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.action_locked"), true);
            return true;
        }
        if (isRopeActive(player)) {
            startRecall(player, false);
            return true;
        }
        if (coreCooldownRemainingTicks(player) > 0) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.core_cooldown"), true);
            return true;
        }
        if (ElectronicInterferenceManager.tryBlockSkillUse(player, ModCharacters.TEMPEST, SkillSlot.CORE)) {
            return true;
        }
        placeAnchor(player);
        triggerExplosiveSpine(player, ModSounds.TEMPEST_EXPLOSIVE_SPINE_ACTIVATE.get());
        return true;
    }

    public static void clearRuntimeOnDeath(ServerPlayer player) {
        setEquippedTool(player, TempestTool.NONE);
        clearRope(player, false);
        data(player).putBoolean(PENDING_LANDING_ROLL, false);
        data(player).putInt(ROLL_BOOST_TICKS, 0);
        data(player).putLong(DOWNED_UNTIL, 0L);
        data(player).putInt(SELF_RESCUE_TICKS, 0);
        player.removeEffect(ModEffects.TEMPEST_EXPLOSIVE_SPINE.get());
        player.removeEffect(ModEffects.TEMPEST_DISARMED.get());
        player.removeEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get());
    }

    public static void triggerExplosiveSpine(ServerPlayer player, SoundEvent sound) {
        if (!isTempest(player)) {
            return;
        }
        boolean refresh = player.hasEffect(ModEffects.TEMPEST_EXPLOSIVE_SPINE.get());
        player.addEffect(new MobEffectInstance(ModEffects.TEMPEST_EXPLOSIVE_SPINE.get(),
                EXPLOSIVE_SPINE_TICKS, 0, false, true, true));
        SoundEvent toPlay = refresh ? ModSounds.TEMPEST_SPEED_REFRESH.get() : sound;
        player.level().playSound(null, player.blockPosition(), toPlay, SoundSource.PLAYERS, 0.72F, refresh ? 1.18F : 1.0F);
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(SPINE_DUST, player.getX(), player.getY() + 0.95D, player.getZ(),
                    12, 0.35D, 0.35D, 0.35D, 0.02D);
        }
    }

    public static void triggerExplosionSpineForNearby(ServerLevel level, Vec3 center, Iterable<Entity> affectedEntities) {
        for (ServerPlayer player : level.players()) {
            if (!isTempest(player)) {
                continue;
            }
            boolean affected = player.position().distanceTo(center) <= 8.0D;
            if (!affected) {
                for (Entity entity : affectedEntities) {
                    if (entity == player) {
                        affected = true;
                        break;
                    }
                }
            }
            if (affected) {
                triggerExplosiveSpine(player, ModSounds.TEMPEST_EXPLOSION_TRIGGER.get());
            }
        }
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isTempest(player)) {
            return;
        }
        initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_SyncTempestState(
                wallCharges(player),
                WALL_MAX_CHARGES,
                wallRechargeRemainingTicks(player),
                rollCooldownRemainingTicks(player),
                coreCooldownRemainingTicks(player),
                equippedTool(player).ordinal(),
                isRopeActive(player),
                ropeRemaining(player),
                isRecalling(player),
                data(player).getBoolean(PENDING_LANDING_ROLL),
                downedRemainingTicks(player),
                data(player).getInt(SELF_RESCUE_TICKS),
                isDowned(player) ? DOWNED_SELF_RESCUE_TICKS : 0
        ), player);
    }

    private static void startClientRoll(ServerPlayer player) {
        NetworkHandler.sendToPlayer(new S2C_TempestStartRoll(), player);
    }

    private static void startRollBoost(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            horizontal = new Vec3(player.getDeltaMovement().x, 0.0D, player.getDeltaMovement().z);
        }
        if (horizontal.lengthSqr() < 0.0001D) {
            horizontal = new Vec3(0.0D, 0.0D, 1.0D);
        }
        Vec3 direction = horizontal.normalize();
        CompoundTag tag = data(player);
        tag.putInt(ROLL_BOOST_TICKS, ROLL_BOOST_TICKS_TOTAL);
        tag.putDouble(ROLL_BOOST_DIR_X, direction.x);
        tag.putDouble(ROLL_BOOST_DIR_Z, direction.z);
    }

    private static void tickRollBoost(ServerPlayer player) {
        CompoundTag tag = data(player);
        int ticks = tag.getInt(ROLL_BOOST_TICKS);
        if (ticks <= 0) {
            return;
        }
        if (player.isInWaterOrBubble() || player.horizontalCollision || isRecalling(player) || isDowned(player)) {
            tag.putInt(ROLL_BOOST_TICKS, 0);
            return;
        }
        Vec3 direction = new Vec3(tag.getDouble(ROLL_BOOST_DIR_X), 0.0D, tag.getDouble(ROLL_BOOST_DIR_Z));
        if (direction.lengthSqr() < 0.0001D) {
            tag.putInt(ROLL_BOOST_TICKS, 0);
            return;
        }
        Vec3 motion = direction.normalize().scale(ROLL_BOOST_SPEED_PER_TICK);
        player.setDeltaMovement(motion.x, player.getDeltaMovement().y, motion.z);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
        tag.putInt(ROLL_BOOST_TICKS, ticks - 1);
    }

    private static void tickPendingLandingRoll(ServerPlayer player) {
        CompoundTag tag = data(player);
        int protection = tag.getInt(FALL_PROTECTION_TICKS);
        if (protection > 0) {
            tag.putInt(FALL_PROTECTION_TICKS, protection - 1);
        }
        if (!tag.getBoolean(PENDING_LANDING_ROLL)) {
            return;
        }
        if (!player.isAlive()
                || player.isInWaterOrBubble()
                || UluruLoiteringMissileEntity.isPlayerControlling(player)
                || RaptorFalconDroneEntity.isPlayerControlling(player)) {
            tag.putBoolean(PENDING_LANDING_ROLL, false);
            return;
        }
        if (player.onGround()) {
            consumeFallProtection(player);
        }
    }

    private static void placeAnchor(ServerPlayer player) {
        CompoundTag tag = data(player);
        Vec3 anchor = player.position();
        tag.putBoolean(ROPE_ACTIVE, true);
        tag.putBoolean(RECALLING, false);
        tag.putBoolean(AUTO_RECALL, false);
        tag.putDouble(ROPE_LENGTH, 0.0D);
        tag.putString(ROPE_DIMENSION, player.level().dimension().location().toString());
        tag.putDouble(FALL_START_Y, player.getY());
        tag.putBoolean(SUSPENDED, false);
        putVec(tag, ANCHOR, anchor);
        ListTag path = new ListTag();
        path.add(vecTag(anchor));
        tag.put(PATH, path);
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_ANCHOR_PLACE.get(),
                SoundSource.PLAYERS, 0.9F, 1.0F);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.anchor_placed"), true);
    }

    private static void startRecall(ServerPlayer player, boolean automatic) {
        CompoundTag tag = data(player);
        if (!tag.getBoolean(ROPE_ACTIVE) || tag.getBoolean(RECALLING)) {
            return;
        }
        appendPathNode(player, player.position(), false);
        ListTag path = tag.getList(PATH, Tag.TAG_COMPOUND);
        tag.putBoolean(RECALLING, true);
        tag.putBoolean(AUTO_RECALL, automatic);
        tag.putInt(RECALL_INDEX, Math.max(0, path.size() - 2));
        player.stopUsingItem();
        player.setSprinting(false);
        player.level().playSound(null, player.blockPosition(), automatic
                        ? ModSounds.TEMPEST_RECALL_LETHAL_TRIGGER.get()
                        : ModSounds.TEMPEST_RECALL_ACTIVE.get(),
                SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static void tickRope(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (!sameDimension(player, tag)) {
            clearRope(player, false);
            return;
        }
        if (!player.onGround()) {
            tag.putDouble(FALL_START_Y, Math.max(tag.getDouble(FALL_START_Y), player.getY()));
            if (!tag.getBoolean(SUSPENDED) && player.fallDistance > 11.0F) {
                suspendFall(player);
            }
        } else {
            tag.putDouble(FALL_START_Y, player.getY());
            tag.putBoolean(SUSPENDED, false);
        }
        appendPathNode(player, player.position(), true);
    }

    private static void suspendFall(ServerPlayer player) {
        CompoundTag tag = data(player);
        double y = Math.max(player.getY(), tag.getDouble(FALL_START_Y) - 10.0D);
        player.teleportTo(player.getX(), y, player.getZ());
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        tag.putBoolean(SUSPENDED, true);
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_ROPE_SUSPEND.get(),
                SoundSource.PLAYERS, 0.9F, 1.0F);
    }

    private static void tickRecall(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (!sameDimension(player, tag)) {
            clearRope(player, true);
            return;
        }
        player.stopUsingItem();
        player.setSprinting(false);
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        if (player.tickCount % 20 == 0) {
            player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_PULL_LOOP.get(),
                    SoundSource.PLAYERS, 0.5F, 1.0F);
        }

        ListTag path = tag.getList(PATH, Tag.TAG_COMPOUND);
        int index = tag.getInt(RECALL_INDEX);
        if (path.isEmpty() || index < 0) {
            finishRecall(player);
            return;
        }

        Vec3 target = vec(path.getCompound(Math.min(index, path.size() - 1)));
        Vec3 current = player.position();
        Vec3 delta = target.subtract(current);
        double distance = delta.length();
        Vec3 next;
        if (distance <= RECALL_SPEED_PER_TICK) {
            next = target;
            tag.putInt(RECALL_INDEX, index - 1);
        } else {
            next = current.add(delta.scale(RECALL_SPEED_PER_TICK / distance));
        }

        if (!canOccupy(player, next)) {
            finishRecallAt(player, findSafeStandPosition(player.serverLevel(), next, anchor(tag)));
            return;
        }
        player.teleportTo(next.x, next.y, next.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        if (tag.getInt(RECALL_INDEX) < 0 || next.distanceTo(anchor(tag)) < 0.75D) {
            finishRecall(player);
        }
    }

    private static void finishRecall(ServerPlayer player) {
        finishRecallAt(player, findSafeStandPosition(player.serverLevel(), anchor(data(player)), anchor(data(player))));
    }

    private static void finishRecallAt(ServerPlayer player, Vec3 destination) {
        CompoundTag tag = data(player);
        boolean automatic = tag.getBoolean(AUTO_RECALL);
        player.teleportTo(destination.x, destination.y, destination.z);
        player.setDeltaMovement(Vec3.ZERO);
        player.fallDistance = 0.0F;
        clearRope(player, true);
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_END.get(),
                SoundSource.PLAYERS, 0.9F, 1.0F);
        if (automatic) {
            enterDowned(player);
        }
    }

    private static void enterDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        long until = player.level().getGameTime() + 60 * 20L;
        tag.putLong(DOWNED_UNTIL, until);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        player.setHealth(Math.max(1.0F, Math.min(player.getHealth(), player.getMaxHealth())));
        player.addEffect(new MobEffectInstance(ModEffects.TEMPEST_EMERGENCY_DOWNED.get(),
                60 * 20, 0, false, true, true));
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_DOWNED.get(),
                SoundSource.PLAYERS, 0.95F, 1.0F);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.downed"), true);
    }

    private static void tickDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        long until = tag.getLong(DOWNED_UNTIL);
        if (until <= 0L) {
            tag.putInt(SELF_RESCUE_TICKS, 0);
            if (player.hasEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get())) {
                player.removeEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get());
            }
            return;
        }
        int remaining = (int) Math.min(Integer.MAX_VALUE, until - player.level().getGameTime());
        if (remaining <= 0) {
            expireDowned(player);
            return;
        }
        if (player.getEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get()) == null
                || player.getEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get()).getDuration() < remaining - 5) {
            player.addEffect(new MobEffectInstance(ModEffects.TEMPEST_EMERGENCY_DOWNED.get(),
                    remaining, 0, false, true, true));
        }
        player.stopUsingItem();
        player.setSprinting(false);
        setEquippedTool(player, TempestTool.NONE);
        if (player.containerMenu != player.inventoryMenu) {
            player.closeContainer();
        }
        if (!player.isShiftKeyDown()) {
            tag.putInt(SELF_RESCUE_TICKS, 0);
            return;
        }
        int ticks = tag.getInt(SELF_RESCUE_TICKS) + 1;
        tag.putInt(SELF_RESCUE_TICKS, ticks);
        if (ticks == 1) {
            player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_SELF_RESCUE_START.get(),
                    SoundSource.PLAYERS, 0.85F, 1.0F);
        }
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.self_rescue",
                progressBar(ticks, DOWNED_SELF_RESCUE_TICKS)), true);
        if (ticks >= DOWNED_SELF_RESCUE_TICKS) {
            tag.putLong(DOWNED_UNTIL, 0L);
            tag.putInt(SELF_RESCUE_TICKS, 0);
            player.removeEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get());
            player.setHealth(Math.max(player.getHealth(), Math.min(player.getMaxHealth(), 6.0F)));
            player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_SELF_RESCUE_COMPLETE.get(),
                    SoundSource.PLAYERS, 0.9F, 1.05F);
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.self_rescue_complete"), true);
        }
    }

    private static void expireDowned(ServerPlayer player) {
        CompoundTag tag = data(player);
        tag.putLong(DOWNED_UNTIL, 0L);
        tag.putInt(SELF_RESCUE_TICKS, 0);
        player.removeEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get());
        player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
    }

    private static int downedRemainingTicks(Player player) {
        long remaining = data(player).getLong(DOWNED_UNTIL) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
    }

    private static void clearRope(ServerPlayer player, boolean startCooldown) {
        CompoundTag tag = data(player);
        tag.putBoolean(ROPE_ACTIVE, false);
        tag.putBoolean(RECALLING, false);
        tag.putBoolean(AUTO_RECALL, false);
        tag.putDouble(ROPE_LENGTH, 0.0D);
        tag.remove(PATH);
        tag.remove(ANCHOR);
        tag.remove(ROPE_DIMENSION);
        tag.putInt(RECALL_INDEX, 0);
        tag.putBoolean(SUSPENDED, false);
        if (startCooldown) {
            tag.putLong(CORE_COOLDOWN_UNTIL,
                    SkillCooldownHelper.until(player, player.level().getGameTime(), CORE_COOLDOWN_TICKS));
        }
    }

    private static void appendPathNode(ServerPlayer player, Vec3 node, boolean enforceLength) {
        CompoundTag tag = data(player);
        ListTag path = tag.getList(PATH, Tag.TAG_COMPOUND);
        if (path.isEmpty()) {
            path.add(vecTag(player.position()));
        }
        Vec3 last = vec(path.getCompound(path.size() - 1));
        double distance = last.distanceTo(node);
        if (distance < PATH_NODE_DISTANCE) {
            return;
        }
        double length = tag.getDouble(ROPE_LENGTH) + distance;
        if (enforceLength && length > ROPE_MAX_LENGTH) {
            player.teleportTo(last.x, last.y, last.z);
            player.setDeltaMovement(Vec3.ZERO);
            warnRope(player);
            return;
        }
        path.add(vecTag(node));
        tag.put(PATH, path);
        tag.putDouble(ROPE_LENGTH, length);
        if (enforceLength && ROPE_MAX_LENGTH - length <= 15.0D) {
            warnRope(player);
        }
    }

    private static void warnRope(ServerPlayer player) {
        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        if (now - tag.getLong(LAST_WARNING_TICK) < 20L) {
            return;
        }
        tag.putLong(LAST_WARNING_TICK, now);
        player.level().playSound(null, player.blockPosition(), ModSounds.TEMPEST_RECALL_ROPE_WARNING.get(),
                SoundSource.PLAYERS, 0.7F, 1.0F);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.tempest.rope_warning",
                Math.round(ropeRemaining(player))), true);
    }

    private static double ropeRemaining(Player player) {
        return Math.max(0.0D, ROPE_MAX_LENGTH - data(player).getDouble(ROPE_LENGTH));
    }

    private static boolean sameDimension(Player player, CompoundTag tag) {
        String dimension = tag.getString(ROPE_DIMENSION);
        return dimension.isEmpty() || dimension.equals(player.level().dimension().location().toString());
    }

    private static boolean canOccupy(ServerPlayer player, Vec3 pos) {
        AABB moved = player.getBoundingBox().move(pos.subtract(player.position()));
        return player.level().noCollision(player, moved);
    }

    private static Vec3 findSafeStandPosition(ServerLevel level, Vec3 preferred, Vec3 fallback) {
        Vec3 safe = findSafeAround(level, preferred, 3);
        if (safe != null) {
            return safe;
        }
        safe = findSafeAround(level, fallback, 5);
        return safe != null ? safe : fallback;
    }

    private static Vec3 findSafeAround(ServerLevel level, Vec3 center, int radius) {
        BlockPos origin = BlockPos.containing(center);
        for (int dy = -2; dy <= 3; dy++) {
            for (int r = 0; r <= radius; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                            continue;
                        }
                        BlockPos feet = origin.offset(dx, dy, dz);
                        if (canStandAt(level, feet)) {
                            return new Vec3(feet.getX() + 0.5D, feet.getY(), feet.getZ() + 0.5D);
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean canStandAt(ServerLevel level, BlockPos feet) {
        BlockPos below = feet.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
    }

    private static void tickProjectileNearMiss(ServerPlayer player, long now) {
        AABB search = player.getBoundingBox().inflate(NEAR_MISS_RADIUS + 0.35D);
        Map<Integer, Long> cooldowns = NEAR_MISS_COOLDOWNS.computeIfAbsent(player.getUUID(), id -> new HashMap<>());
        cooldowns.entrySet().removeIf(entry -> entry.getValue() <= now);
        Vec3 center = player.position().add(0.0D, player.getBbHeight() * 0.55D, 0.0D);
        for (Entity entity : player.level().getEntities(player, search, TempestStateManager::isDangerousIncomingEntity)) {
            if (cooldowns.containsKey(entity.getId())) {
                continue;
            }
            if (entity instanceof Projectile projectile && projectile.getOwner() == player) {
                continue;
            }
            Vec3 motion = entity.getDeltaMovement();
            if (motion.lengthSqr() < 0.0025D) {
                continue;
            }
            Vec3 end = entity.position();
            Vec3 start = end.subtract(motion);
            if (distancePointToSegment(center, start, end) <= NEAR_MISS_RADIUS) {
                cooldowns.put(entity.getId(), now + 20L);
                triggerExplosiveSpine(player, ModSounds.TEMPEST_DANGER_NEAR_MISS.get());
                break;
            }
        }
    }

    private static boolean isDangerousIncomingEntity(Entity entity) {
        if (!entity.isAlive()) {
            return false;
        }
        String name = entity.getClass().getSimpleName().toLowerCase();
        if (name.contains("smoke") || name.contains("medical") || name.contains("healing")
                || name.contains("sonar") || name.contains("recon") || name.contains("decoy")
                || name.contains("marker") || name.contains("cloud") || name.contains("dust")) {
            return false;
        }
        return entity instanceof Projectile
                || name.contains("grenade")
                || name.contains("missile")
                || name.contains("knife")
                || name.contains("bomb")
                || name.contains("arrow")
                || name.contains("orb")
                || name.contains("stinger")
                || name.contains("cannon");
    }

    private static double distancePointToSegment(Vec3 point, Vec3 start, Vec3 end) {
        Vec3 line = end.subtract(start);
        double lengthSqr = line.lengthSqr();
        if (lengthSqr < 1.0E-6D) {
            return point.distanceTo(end);
        }
        double t = point.subtract(start).dot(line) / lengthSqr;
        t = Math.max(0.0D, Math.min(1.0D, t));
        return point.distanceTo(start.add(line.scale(t)));
    }

    private static void recharge(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        int charges = tag.getInt(WALL_CHARGES);
        if (charges >= WALL_MAX_CHARGES) {
            tag.putLong(WALL_NEXT_RECHARGE, 0L);
            return;
        }
        long next = tag.getLong(WALL_NEXT_RECHARGE);
        if (next <= 0L) {
            tag.putLong(WALL_NEXT_RECHARGE, SkillCooldownHelper.until(player, now, WALL_RECHARGE_TICKS));
            return;
        }
        while (charges < WALL_MAX_CHARGES && now >= next) {
            charges++;
            next += SkillCooldownHelper.ticks(player, WALL_RECHARGE_TICKS);
        }
        tag.putInt(WALL_CHARGES, charges);
        tag.putLong(WALL_NEXT_RECHARGE, charges >= WALL_MAX_CHARGES ? 0L : next);
    }

    private static int remainingTicks(Player player, String key) {
        long remaining = data(player).getLong(key) - player.level().getGameTime();
        return remaining > 0L ? (int) Math.min(Integer.MAX_VALUE, remaining) : 0;
    }

    private static void putVec(CompoundTag tag, String key, Vec3 vec) {
        tag.put(key, vecTag(vec));
    }

    private static Vec3 anchor(CompoundTag tag) {
        return tag.contains(ANCHOR, Tag.TAG_COMPOUND) ? vec(tag.getCompound(ANCHOR)) : Vec3.ZERO;
    }

    private static CompoundTag vecTag(Vec3 vec) {
        CompoundTag tag = new CompoundTag();
        tag.putDouble("X", vec.x);
        tag.putDouble("Y", vec.y);
        tag.putDouble("Z", vec.z);
        return tag;
    }

    private static Vec3 vec(CompoundTag tag) {
        return new Vec3(tag.getDouble("X"), tag.getDouble("Y"), tag.getDouble("Z"));
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
