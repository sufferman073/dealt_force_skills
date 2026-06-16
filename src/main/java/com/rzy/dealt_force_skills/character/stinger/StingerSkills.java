package com.rzy.dealt_force_skills.character.stinger;

import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.entity.StingerSmokeDroneEntity;
import com.rzy.dealt_force_skills.entity.StingerSmokeGrenadeEntity;
import com.rzy.dealt_force_skills.entity.StingerStimProjectileEntity;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_StingerStimLockStatus;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public final class StingerSkills {
    private static final double STIM_LOCK_RANGE = 64.0D;
    private static final double STIM_LOCK_MIN_ALIGNMENT = 0.78D;
    private static final double STIM_LOCK_DIRECT_ALIGNMENT = 0.975D;
    private static final double STIM_LOCK_MAX_OFF_AXIS = 2.0D;
    private static final int STIM_LOCK_SYNC_INTERVAL_TICKS = 5;
    private static final int STIM_LOCK_STATUS_TICKS = 8;
    private static final double STIM_PROJECTILE_SPEED = 2.25D;

    private StingerSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        StingerStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> throwSmokeGrenade(player, false);
            case ACTIVE_2 -> alternate ? launchSmokeDroneDirect(player, true) : toggleSmokeDrone(player);
            case CORE -> alternate ? selfStim(player) : toggleStimGun(player);
            case PASSIVE -> false;
        };
    }

    public static void tickStimLockStatus(ServerPlayer player) {
        if (player.tickCount % STIM_LOCK_SYNC_INTERVAL_TICKS != 0) {
            return;
        }

        StingerStimMode mode = StingerStateManager.stimMode(player);
        if (StingerStateManager.equippedTool(player) != StingerTool.STIM_GUN
                || StingerStateManager.stimCharges(player) <= 0) {
            NetworkHandler.sendToPlayer(new S2C_StingerStimLockStatus(false, mode.ordinal(), 0, STIM_LOCK_STATUS_TICKS), player);
            return;
        }

        List<ServerPlayer> targets = lockedPlayers(player);
        NetworkHandler.sendToPlayer(new S2C_StingerStimLockStatus(false, mode.ordinal(), targets.size(), STIM_LOCK_STATUS_TICKS), player);
        for (ServerPlayer target : targets) {
            NetworkHandler.sendToPlayer(new S2C_StingerStimLockStatus(true, mode.ordinal(), 0, STIM_LOCK_STATUS_TICKS), target);
        }
    }

    public static boolean handleToolAction(ServerPlayer player, StingerToolAction action, boolean alternate) {
        if (!StingerStateManager.isStinger(player)) {
            return false;
        }
        StingerStateManager.initializeIfNeeded(player);

        return switch (action) {
            case STOW_TOOL -> {
                StingerStateManager.setEquippedTool(player, StingerTool.NONE);
                yield true;
            }
            case EQUIP_SMOKE_GRENADE -> equipSmokeGrenade(player);
            case THROW_SMOKE_GRENADE -> throwSmokeGrenade(player, true);
            case LAUNCH_SMOKE_DRONE -> launchSmokeDrone(player, alternate);
            case FIRE_STIM_GUN -> fireStimGun(player);
            case TOGGLE_STIM_MODE -> toggleStimMode(player);
            case SELF_STIM -> selfStim(player);
            case STOP_SMOKE_DRONE_GUIDE -> {
                StingerSmokeDroneEntity.stopGuidingFor(player);
                yield true;
            }
        };
    }

    private static boolean equipSmokeGrenade(ServerPlayer player) {
        if (!StingerStateManager.smokeReady(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.smoke_cooldown"), true);
            return true;
        }
        StingerStateManager.setEquippedTool(player, StingerTool.SMOKE_GRENADE);
        player.level().playSound(null, player.blockPosition(), ModSounds.STINGER_SMOKE_EQUIP.get(),
                SoundSource.PLAYERS, 0.75f, 1.0f);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.smoke_equipped"), true);
        return true;
    }

    private static boolean throwSmokeGrenade(ServerPlayer player, boolean highThrow) {
        if (!StingerStateManager.consumeSmoke(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.smoke_cooldown"), true);
            return true;
        }

        ServerLevel level = player.serverLevel();
        StingerSmokeGrenadeEntity smoke = new StingerSmokeGrenadeEntity(ModEntities.STINGER_SMOKE_GRENADE.get(), level, player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(0.58D));
        double speed = highThrow ? 1.8D : 1.05D;
        double lift = highThrow ? 0.34D : 0.08D;
        smoke.setPos(start.x, start.y - 0.1D, start.z);
        smoke.setDeltaMovement(look.scale(speed).add(0.0D, lift, 0.0D));
        smoke.setYRot(player.getYRot());
        smoke.setXRot(player.getXRot());
        level.addFreshEntity(smoke);
        level.playSound(null, player.blockPosition(), ModSounds.STINGER_SMOKE_THROW.get(),
                SoundSource.PLAYERS, 0.95f, highThrow ? 0.95f : 1.1f);
        StingerStateManager.setEquippedTool(player, StingerTool.NONE);
        return true;
    }

    private static boolean toggleSmokeDrone(ServerPlayer player) {
        if (StingerStateManager.equippedTool(player) == StingerTool.SMOKE_DRONE) {
            StingerStateManager.setEquippedTool(player, StingerTool.NONE);
            return true;
        }
        if (!StingerStateManager.droneReady(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.drone_cooldown"), true);
            return true;
        }

        StingerStateManager.setEquippedTool(player, StingerTool.SMOKE_DRONE);
        player.level().playSound(null, player.blockPosition(), ModSounds.STINGER_DRONE_EQUIP.get(),
                SoundSource.PLAYERS, 0.75f, 1.0f);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.drone_equipped"), true);
        return true;
    }

    private static boolean launchSmokeDrone(ServerPlayer player, boolean guided) {
        return launchSmokeDrone(player, guided, true);
    }

    private static boolean launchSmokeDroneDirect(ServerPlayer player, boolean guided) {
        return launchSmokeDrone(player, guided, false);
    }

    private static boolean launchSmokeDrone(ServerPlayer player, boolean guided, boolean requireEquipped) {
        if (requireEquipped && StingerStateManager.equippedTool(player) != StingerTool.SMOKE_DRONE) {
            return false;
        }
        if (!StingerStateManager.consumeDrone(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.drone_cooldown"), true);
            if (requireEquipped) {
                StingerStateManager.setEquippedTool(player, StingerTool.NONE);
            }
            return true;
        }

        ServerLevel level = player.serverLevel();
        StingerSmokeDroneEntity drone = new StingerSmokeDroneEntity(ModEntities.STINGER_SMOKE_DRONE.get(), level, player, guided);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(0.8D));
        drone.setPos(start.x, start.y - 0.1D, start.z);
        level.addFreshEntity(drone);
        level.playSound(null, player.blockPosition(), ModSounds.STINGER_DRONE_LAUNCH.get(),
                SoundSource.PLAYERS, 0.9f, guided ? 0.95f : 1.05f);
        StingerStateManager.setEquippedTool(player, StingerTool.NONE);
        player.displayClientMessage(Component.translatable(guided
                ? "message.dealt_force_skills.stinger.drone_guided"
                : "message.dealt_force_skills.stinger.drone_launch"), true);
        return true;
    }

    private static boolean toggleStimGun(ServerPlayer player) {
        if (StingerStateManager.equippedTool(player) == StingerTool.STIM_GUN) {
            StingerStateManager.setEquippedTool(player, StingerTool.NONE);
            return true;
        }
        StingerStateManager.setEquippedTool(player, StingerTool.STIM_GUN);
        player.level().playSound(null, player.blockPosition(), ModSounds.STINGER_STIM_EQUIP.get(),
                SoundSource.PLAYERS, 0.75f, 1.0f);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.stim_equipped"), true);
        return true;
    }

    private static boolean selfStim(ServerPlayer player) {
        if (!StingerStateManager.consumeStimCharge(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.stim_empty"), true);
            return true;
        }
        StingerStateManager.applyStimHeal(player);
        player.level().playSound(null, player.blockPosition(), ModSounds.STINGER_STIM_FIRE.get(),
                SoundSource.PLAYERS, 0.85f, 1.08f);
        player.level().playSound(null, player.blockPosition(), ModSounds.STINGER_STIM_HIT.get(),
                SoundSource.PLAYERS, 0.75f, 1.15f);
        return true;
    }

    private static boolean toggleStimMode(ServerPlayer player) {
        if (StingerStateManager.equippedTool(player) != StingerTool.STIM_GUN) {
            return false;
        }
        StingerStateManager.toggleStimMode(player);
        player.level().playSound(null, player.blockPosition(), ModSounds.STINGER_STIM_MODE.get(),
                SoundSource.PLAYERS, 0.65f, StingerStateManager.stimMode(player) == StingerStimMode.HEAL ? 1.2f : 0.85f);
        player.displayClientMessage(Component.translatable(StingerStateManager.stimMode(player) == StingerStimMode.HEAL
                ? "message.dealt_force_skills.stinger.mode_heal"
                : "message.dealt_force_skills.stinger.mode_suppress"), true);
        return true;
    }

    private static boolean fireStimGun(ServerPlayer player) {
        if (StingerStateManager.equippedTool(player) != StingerTool.STIM_GUN) {
            return false;
        }
        if (StingerStateManager.stimCharges(player) <= 0) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.stim_empty"), true);
            return true;
        }

        StingerStimMode mode = StingerStateManager.stimMode(player);
        List<ServerPlayer> targets = lockedPlayers(player);
        if (targets.isEmpty()) {
            if (StingerStateManager.consumeStimCharge(player)) {
                spawnStimProjectile(player, null, mode);
            }
            return true;
        }

        int fired = 0;
        for (ServerPlayer target : targets) {
            if (!StingerStateManager.consumeStimCharge(player)) {
                break;
            }
            spawnStimProjectile(player, target, mode);
            fired++;
        }
        if (fired <= 0) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.stinger.stim_empty"), true);
        }
        return true;
    }

    private static void spawnStimProjectile(ServerPlayer player, ServerPlayer target, StingerStimMode mode) {
        ServerLevel level = player.serverLevel();
        StingerStimProjectileEntity projectile = new StingerStimProjectileEntity(
                ModEntities.STINGER_STIM_PROJECTILE.get(),
                level,
                player,
                target == null ? null : target.getUUID(),
                mode
        );
        Vec3 look = player.getLookAngle().normalize();
        Vec3 right = look.cross(new Vec3(0.0D, 1.0D, 0.0D)).normalize();
        Vec3 vertical = right.cross(look).normalize();
        Vec3 start = player.getEyePosition()
                .add(look.scale(0.58D))
                .add(right.scale(0.30D))
                .add(vertical.scale(-0.24D));
        Vec3 direction = target == null
                ? look
                : target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D).subtract(start).normalize();
        projectile.setPos(start.x, start.y, start.z);
        projectile.setDeltaMovement(direction.scale(STIM_PROJECTILE_SPEED));
        projectile.setYRot(player.getYRot());
        projectile.setXRot(player.getXRot());
        level.addFreshEntity(projectile);
        level.playSound(null, player.blockPosition(), ModSounds.STINGER_STIM_FIRE.get(),
                SoundSource.PLAYERS, 0.85f, mode == StingerStimMode.HEAL ? 1.12f : 0.88f);
    }

    private static List<ServerPlayer> lockedPlayers(ServerPlayer player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();
        double rangeSqr = STIM_LOCK_RANGE * STIM_LOCK_RANGE;
        return player.serverLevel().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(STIM_LOCK_RANGE),
                        target -> target != player
                                && TargetingUtil.isTargetablePlayer(target)
                                && target.distanceToSqr(player) <= rangeSqr
                                && isInStimLockCone(eye, look, target))
                .stream()
                .sorted(Comparator.comparingDouble(player::distanceToSqr))
                .toList();
    }

    private static boolean isInStimLockCone(Vec3 eye, Vec3 look, LivingEntity target) {
        Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.58D, 0.0D);
        Vec3 toTarget = targetCenter.subtract(eye);
        double distance = toTarget.length();
        if (distance < 0.001D) {
            return true;
        }
        Vec3 direction = toTarget.scale(1.0D / distance);
        double alignment = look.dot(direction);
        if (alignment < STIM_LOCK_MIN_ALIGNMENT) {
            return false;
        }
        double offAxis = Math.sqrt(Math.max(0.0D, 1.0D - alignment * alignment)) * distance;
        return alignment >= STIM_LOCK_DIRECT_ALIGNMENT || offAxis <= STIM_LOCK_MAX_OFF_AXIS;
    }
}
