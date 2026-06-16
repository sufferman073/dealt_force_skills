package com.rzy.dealt_force_skills.character.sineva;

import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import com.rzy.dealt_force_skills.entity.BladeWireProjectileEntity;
import com.rzy.dealt_force_skills.entity.GrappleHookEntity;
import com.rzy.dealt_force_skills.registry.ModBlocks;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SinevaSkills {
    private static final double FRONT_DOT = Math.cos(Math.toRadians(60.0));
    private static final int BASH_COOLDOWN_TICKS = 10;
    private static final int CHARGE_COOLDOWN_TICKS = 20;
    private static final int BASH_ACTIVE_STAMINA_TICKS = 20;
    private static final int SILENCE_TICKS = 60;
    private static final double PROJECTILE_REFLECT_MIN_SPEED = 1.4D;
    private static final double PROJECTILE_REFLECT_MAX_SPEED = 3.8D;
    private static final double PROJECTILE_CHARGE_SPEED_BONUS = 0.45D;
    private static final ConcurrentHashMap<UUID, ChargeData> CHARGING_PLAYERS = new ConcurrentHashMap<>();

    private SinevaSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        SinevaStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> useBladeWire(player, alternate);
            case ACTIVE_2 -> useGrapple(player);
            case CORE -> useBombSuit(player, alternate);
            case PASSIVE -> false;
        };
    }

    public static void deployBladeWire(ServerLevel level, BlockPos center, ServerPlayer owner) {
        BlockPos corePos = findGroundPlacement(level, center);
        if (corePos == null) {
            return;
        }

        level.setBlock(corePos, ModBlocks.BLADE_WIRE_CORE.get().defaultBlockState(), 3);
        if (level.getBlockEntity(corePos) instanceof com.rzy.dealt_force_skills.block.BladeWireBlockEntity core) {
            core.configureCore(owner.getUUID());
        }

        int placedWires = 0;
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                if (dx == 0 && dz == 0) continue;
                if (dx * dx + dz * dz > 16) continue;

                BlockPos wirePos = findGroundPlacement(level, corePos.offset(dx, 0, dz));
                if (wirePos == null || wirePos.equals(corePos) || !level.getBlockState(wirePos).canBeReplaced()) {
                    continue;
                }

                level.setBlock(wirePos, ModBlocks.BLADE_WIRE.get().defaultBlockState(), 3);
                if (level.getBlockEntity(wirePos) instanceof com.rzy.dealt_force_skills.block.BladeWireBlockEntity wire) {
                    wire.configureWire(owner.getUUID(), corePos);
                    placedWires++;
                }
            }
        }

        if (level.getBlockEntity(corePos) instanceof com.rzy.dealt_force_skills.block.BladeWireBlockEntity core) {
            core.setLinkedWireCount(placedWires);
        }

        level.playSound(null, corePos, ModSounds.WIRE_PLACE.get(), SoundSource.BLOCKS, 1.0f, 1.0f);
        damageBladeWireBurst(level, corePos, owner);
    }

    public static boolean tryShieldBash(ServerPlayer player) {
        if (!canUseShieldAction(player) || !SinevaStateManager.isBashReady(player)) {
            return false;
        }

        SinevaStateManager.setBashCooldown(player, BASH_COOLDOWN_TICKS);
        ParcoolStaminaBridge.markActiveStaminaUse(player, BASH_ACTIVE_STAMINA_TICKS);
        player.level().playSound(null, player.blockPosition(), ModSounds.SHIELD_BASH.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);

        Vec3 forward = player.getLookAngle().normalize();
        AABB box = player.getBoundingBox().inflate(2.0, 1.0, 2.0);
        reflectProjectiles(player, box, forward, FRONT_DOT, null, 0.0D);
        boolean controlVoicePlayed = false;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != player && entity.isAlive())) {
            Vec3 toTarget = target.position().add(0, target.getBbHeight() * 0.5, 0)
                    .subtract(player.getEyePosition()).normalize();
            if (forward.dot(toTarget) < FRONT_DOT) continue;

            float damage = target instanceof Player ? 5.0f : 50.0f;
            if (SkillDamageHelper.hurt(target, SkillDamageHelper.sinevaShieldBash(player.serverLevel(), null, player), player, damage)) {
                player.level().playSound(null, target.blockPosition(), ModSounds.SINEVA_SHIELD_HIT.get(),
                        SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            if (applyShieldBashControl(player, target) && !controlVoicePlayed) {
                playShieldBashControlVoice(player);
                controlVoicePlayed = true;
            }
        }
        return true;
    }

    public static boolean tryShieldCharge(ServerPlayer player) {
        if (!canUseShieldAction(player) || !SinevaStateManager.isChargeReady(player)) {
            return false;
        }
        if (CHARGING_PLAYERS.containsKey(player.getUUID())) {
            return true;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0, look.z);
        if (horizontal.lengthSqr() < 0.001) {
            return true;
        }

        SinevaStateManager.setChargeCooldown(player, CHARGE_COOLDOWN_TICKS);
        CHARGING_PLAYERS.put(player.getUUID(), new ChargeData(horizontal.normalize(), 14));
        player.level().playSound(null, player.blockPosition(), ModSounds.SHIELD_BASH.get(),
                SoundSource.PLAYERS, 1.0f, 0.8f);
        return true;
    }

    public static void tickChargingPlayers(Level level) {
        if (level.isClientSide) return;

        var iterator = CHARGING_PLAYERS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(entry.getKey());
            if (player == null) {
                iterator.remove();
                continue;
            }

            ChargeData data = entry.getValue();
            if (!canUseShieldAction(player) || data.ticksRemaining-- <= 0 || player.horizontalCollision) {
                iterator.remove();
                continue;
            }

            Vec3 motion = data.direction.scale(1.65);
            player.setDeltaMovement(motion.x, player.getDeltaMovement().y, motion.z);
            player.hurtMarked = true;

            AABB box = player.getBoundingBox().inflate(2.0, 1.0, 2.0);
            reflectProjectiles(player, box, data.direction, 0.0D, data.reflectedProjectiles, PROJECTILE_CHARGE_SPEED_BONUS);
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box,
                    entity -> entity != player && entity.isAlive() && !data.hitEntities.contains(entity))) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                if (data.direction.dot(new Vec3(toTarget.x, 0.0, toTarget.z).normalize()) < 0.0) continue;

                float damage = target instanceof Player ? 5.0f : 50.0f;
                if (SkillDamageHelper.hurt(target, SkillDamageHelper.sinevaShieldBash(player.serverLevel(), null, player), player, damage)) {
                    player.level().playSound(null, target.blockPosition(), ModSounds.SINEVA_SHIELD_HIT.get(),
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                if (applyShieldBashControl(player, target) && !data.controlVoicePlayed) {
                    playShieldBashControlVoice(player);
                    data.controlVoicePlayed = true;
                }
                data.hitEntities.add(target);
            }
        }
    }

    private static boolean applyShieldBashControl(ServerPlayer player, LivingEntity target) {
        if (!target.isAlive()) {
            return false;
        }
        target.addEffect(new MobEffectInstance(ModEffects.STUN.get(), SILENCE_TICKS, 0, false, true));
        SinevaKnockdownState.apply(player, target, SILENCE_TICKS);
        return true;
    }

    private static void playShieldBashControlVoice(ServerPlayer player) {
        player.level().playSound(null, player.blockPosition(), ModSounds.SINEVA_SHIELD_BASH_CONTROL.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void reflectProjectiles(ServerPlayer player, AABB box, Vec3 facing, double minDot,
                                           Set<UUID> alreadyReflected, double speedBonus) {
        Vec3 shieldFacing = normalizeOrFallback(facing, player.getLookAngle());
        for (Projectile projectile : player.level().getEntitiesOfClass(Projectile.class, box, candidate ->
                candidate.isAlive() && candidate.getOwner() != player)) {
            UUID projectileId = projectile.getUUID();
            if (alreadyReflected != null && alreadyReflected.contains(projectileId)) {
                continue;
            }

            Vec3 toProjectile = projectile.position().add(0.0D, projectile.getBbHeight() * 0.5D, 0.0D)
                    .subtract(player.getEyePosition());
            Vec3 fallbackDirection = normalizeOrFallback(toProjectile, shieldFacing);
            if (shieldFacing.dot(fallbackDirection) < minDot) {
                continue;
            }

            reflectProjectile(player, projectile, fallbackDirection, speedBonus);
            if (alreadyReflected != null) {
                alreadyReflected.add(projectileId);
            }
        }
    }

    private static void reflectProjectile(ServerPlayer player, Projectile projectile, Vec3 fallbackDirection, double speedBonus) {
        Vec3 incomingMotion = projectile.getDeltaMovement();
        double incomingSpeed = incomingMotion.length();
        double reflectSpeed = Math.min(PROJECTILE_REFLECT_MAX_SPEED,
                Math.max(PROJECTILE_REFLECT_MIN_SPEED, incomingSpeed + speedBonus));
        Vec3 reflectedMotion = incomingSpeed > 1.0E-4D
                ? incomingMotion.normalize().scale(-reflectSpeed)
                : fallbackDirection.scale(reflectSpeed);
        if (reflectedMotion.dot(fallbackDirection) < 0.15D) {
            reflectedMotion = fallbackDirection.scale(reflectSpeed);
        }

        projectile.setOwner(player);
        projectile.setDeltaMovement(reflectedMotion);
        alignProjectileRotation(projectile, reflectedMotion);
        projectile.hasImpulse = true;
        projectile.hurtMarked = true;
        player.level().playSound(null, projectile.blockPosition(), ModSounds.SINEVA_PROJECTILE_REFLECT.get(),
                SoundSource.PLAYERS, 0.9f, 1.0f);
    }

    private static void alignProjectileRotation(Projectile projectile, Vec3 motion) {
        double horizontal = motion.horizontalDistance();
        projectile.setYRot((float) (Math.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
        projectile.setXRot((float) (Math.atan2(motion.y, horizontal) * -(180.0D / Math.PI)));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
    }

    private static Vec3 normalizeOrFallback(Vec3 vector, Vec3 fallback) {
        if (vector.lengthSqr() >= 1.0E-6D) {
            return vector.normalize();
        }
        return fallback.lengthSqr() >= 1.0E-6D ? fallback.normalize() : new Vec3(0.0D, 0.0D, 1.0D);
    }

    public static boolean canUseShieldAction(Player player) {
        return SinevaStateManager.isShieldDeployed(player) && !player.hasEffect(ModEffects.STUN.get());
    }

    public static void clearForPlayer(ServerPlayer player) {
        CHARGING_PLAYERS.remove(player.getUUID());
    }

    private static boolean useBladeWire(ServerPlayer player, boolean heldThrow) {
        if (!SinevaStateManager.consumeBladeWireCharge(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.sineva.blade_wire_empty"), true);
            return true;
        }

        ServerLevel level = player.serverLevel();
        BladeWireProjectileEntity projectile = new BladeWireProjectileEntity(ModEntities.BLADE_WIRE_PROJECTILE.get(), level, player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(heldThrow ? 0.55D : 0.6D));
        projectile.setPos(start.x, start.y - 0.1, start.z);
        projectile.setDeltaMovement(heldThrow ? look.scale(2.25D).add(0.0D, 0.14D, 0.0D) : look.scale(1.25D));
        projectile.setYRot(player.getYRot());
        projectile.setXRot(player.getXRot());
        level.addFreshEntity(projectile);
        return true;
    }

    private static boolean useGrapple(ServerPlayer player) {
        if (!SinevaStateManager.isGrappleReady(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.sineva.grapple_cooldown"), true);
            return true;
        }

        ServerLevel level = player.serverLevel();
        GrappleHookEntity hook = new GrappleHookEntity(ModEntities.GRAPPLE_HOOK.get(), level, player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = GrappleHookEntity.ropeOrigin(player, 1.0F);
        hook.setPos(start.x, start.y, start.z);
        hook.setDeltaMovement(look.scale(2.2));
        hook.setYRot(player.getYRot());
        hook.setXRot(player.getXRot());
        level.addFreshEntity(hook);
        level.playSound(null, player.blockPosition(), ModSounds.GRAPPLE_FIRE.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        SinevaStateManager.setGrappleCooldown(player);
        return true;
    }

    private static boolean useBombSuit(ServerPlayer player, boolean alternate) {
        if (SinevaStateManager.isBombSuitActive(player)) {
            if (alternate) {
                SinevaStateManager.deactivateBombSuit(player);
                player.displayClientMessage(Component.translatable("message.dealt_force_skills.sineva.bomb_suit_deactivated"), true);
            } else {
                boolean deployed = !SinevaStateManager.isShieldDeployed(player);
                SinevaStateManager.setShieldDeployed(player, deployed);
                player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE,
                        SoundSource.PLAYERS, 0.85f, deployed ? 1.08f : 0.82f);
                player.displayClientMessage(Component.translatable(deployed
                        ? "message.dealt_force_skills.sineva.shield_deployed"
                        : "message.dealt_force_skills.sineva.shield_stowed"), true);
            }
            return true;
        }

        if (SinevaStateManager.isBombSuitEquipping(player)) {
            return true;
        }

        if (!SinevaStateManager.isBombSuitCooldownReady(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.sineva.bomb_suit_cooldown"), true);
            return true;
        }

        SinevaStateManager.startBombSuitEquip(player);
        player.level().playSound(null, player.blockPosition(), ModSounds.BOMB_SUIT_EQUIP.get(),
                SoundSource.PLAYERS, 0.9f, 0.95f);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.sineva.bomb_suit_equipping"), true);
        return true;
    }

    private static void damageBladeWireBurst(ServerLevel level, BlockPos corePos, ServerPlayer owner) {
        Vec3 center = Vec3.atCenterOf(corePos);
        AABB box = new AABB(corePos).inflate(4.0);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box,
                entity -> entity != owner && entity.isAlive() && entity.distanceToSqr(center) <= 16.0)) {
            SkillDamageHelper.hurt(target, SkillDamageHelper.sinevaBladeWire(level, null, owner), owner, 12.0f);
        }
        // Blade wire explosion should destroy Uluru quick covers in range
        com.rzy.dealt_force_skills.character.uluru.UluruExplosionHelper.destroyQuickCovers(level, center, 5.0);
    }

    private static BlockPos findGroundPlacement(ServerLevel level, BlockPos preferred) {
        for (int dy = 1; dy >= -8; dy--) {
            BlockPos pos = preferred.offset(0, dy, 0);
            if (level.getBlockState(pos).canBeReplaced() && hasGround(level, pos)) {
                return pos;
            }
        }
        return null;
    }

    private static boolean hasGround(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return Block.canSupportCenter(level, below, Direction.UP)
                || !level.getBlockState(below).getCollisionShape(level, below).isEmpty();
    }

    private static final class ChargeData {
        private final Vec3 direction;
        private final Set<LivingEntity> hitEntities = new HashSet<>();
        private final Set<UUID> reflectedProjectiles = new HashSet<>();
        private boolean controlVoicePlayed;
        private int ticksRemaining;

        private ChargeData(Vec3 direction, int ticksRemaining) {
            this.direction = direction;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
