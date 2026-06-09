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
            applyShieldBashControl(player, target);
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
            for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box,
                    entity -> entity != player && entity.isAlive() && !data.hitEntities.contains(entity))) {
                Vec3 toTarget = target.position().subtract(player.position()).normalize();
                if (data.direction.dot(new Vec3(toTarget.x, 0.0, toTarget.z).normalize()) < 0.0) continue;

                float damage = target instanceof Player ? 5.0f : 50.0f;
                if (SkillDamageHelper.hurt(target, SkillDamageHelper.sinevaShieldBash(player.serverLevel(), null, player), player, damage)) {
                    player.level().playSound(null, target.blockPosition(), ModSounds.SINEVA_SHIELD_HIT.get(),
                            SoundSource.PLAYERS, 1.0f, 1.0f);
                }
                applyShieldBashControl(player, target);
                data.hitEntities.add(target);
            }
        }
    }

    private static void applyShieldBashControl(ServerPlayer player, LivingEntity target) {
        if (!target.isAlive()) {
            return;
        }
        target.addEffect(new MobEffectInstance(ModEffects.STUN.get(), SILENCE_TICKS, 0, false, true));
        SinevaKnockdownState.apply(player, target, SILENCE_TICKS);
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
        Vec3 start = player.getEyePosition().add(look.scale(0.6));
        hook.setPos(start.x, start.y - 0.1, start.z);
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
        private int ticksRemaining;

        private ChargeData(Vec3 direction, int ticksRemaining) {
            this.direction = direction;
            this.ticksRemaining = ticksRemaining;
        }
    }
}
