package com.rzy.dealt_force_skills.character.vyron;

import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.entity.VyronMagneticBombEntity;
import com.rzy.dealt_force_skills.entity.VyronTigerCannonEntity;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.util.RangedSoundHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public final class VyronSkills {
    private VyronSkills() {
    }

    public static boolean useSkill(ServerPlayer player, SkillSlot slot, boolean alternate) {
        VyronStateManager.initializeIfNeeded(player);
        return switch (slot) {
            case ACTIVE_1 -> tryDash(player, player.getLookAngle());
            case ACTIVE_2 -> quickThrowMagneticBomb(player);
            case CORE -> toggleTigerCannon(player);
            case PASSIVE -> false;
        };
    }

    public static boolean tryDash(ServerPlayer player, Vec3 direction) {
        if (!VyronStateManager.startDash(player, direction)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.dash_cooldown"), true);
            return true;
        }
        return true;
    }

    public static boolean handleToolAction(ServerPlayer player, VyronToolAction action, boolean highThrow) {
        if (!VyronStateManager.isVyron(player)) {
            return false;
        }
        VyronStateManager.initializeIfNeeded(player);

        return switch (action) {
            case EQUIP_MAGNETIC_BOMB -> equipMagneticBomb(player);
            case THROW_MAGNETIC_BOMB -> throwMagneticBomb(player, highThrow);
            case FIRE_TIGER_CANNON -> fireTigerCannon(player);
            case STOW_TOOL -> {
                VyronStateManager.setEquippedTool(player, VyronTool.NONE);
                yield true;
            }
        };
    }

    public static void handleKill(ServerPlayer player) {
        VyronStateManager.resetDashCooldown(player);
    }

    private static boolean quickThrowMagneticBomb(ServerPlayer player) {
        return throwMagneticBomb(player, false);
    }

    private static boolean equipMagneticBomb(ServerPlayer player) {
        if (VyronStateManager.equippedTool(player) == VyronTool.MAGNETIC_BOMB) {
            return true;
        }
        if (VyronStateManager.bombCharges(player) <= 0) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.bomb_empty"), true);
            return true;
        }
        VyronStateManager.setEquippedTool(player, VyronTool.MAGNETIC_BOMB);
        RangedSoundHelper.playThrottled(player.serverLevel(), player.position(), ModSounds.VYRON_MAGNETIC_BOMB_EQUIP.get(),
                SoundSource.PLAYERS, 1.15f, 1.0f, 20.0D, 5, 3.0D);
        return true;
    }

    private static boolean throwMagneticBomb(ServerPlayer player, boolean highThrow) {
        if (highThrow && VyronStateManager.equippedTool(player) != VyronTool.MAGNETIC_BOMB) {
            return true;
        }
        if (!VyronStateManager.consumeBombCharge(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.bomb_empty"), true);
            VyronStateManager.setEquippedTool(player, VyronTool.NONE);
            return true;
        }

        ServerLevel level = player.serverLevel();
        VyronMagneticBombEntity bomb = new VyronMagneticBombEntity(ModEntities.VYRON_MAGNETIC_BOMB.get(), level, player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(0.62D));
        double speed = highThrow ? 1.75D : 1.08D;
        double lift = highThrow ? 0.32D : 0.06D;
        bomb.setPos(start.x, start.y - 0.1D, start.z);
        bomb.setDeltaMovement(look.scale(speed).add(0.0D, lift, 0.0D));
        bomb.setYRot(player.getYRot());
        bomb.setXRot(player.getXRot());
        level.addFreshEntity(bomb);
        RangedSoundHelper.playThrottled(level, player.position(), ModSounds.VYRON_MAGNETIC_BOMB_THROW.get(),
                SoundSource.PLAYERS, 1.2f, highThrow ? 0.95f : 1.08f, 20.0D, 5, 3.0D);
        VyronStateManager.grantPowered(player, false);
        VyronStateManager.setEquippedTool(player, VyronTool.NONE);
        return true;
    }

    private static boolean toggleTigerCannon(ServerPlayer player) {
        if (VyronStateManager.equippedTool(player) == VyronTool.TIGER_CANNON) {
            VyronStateManager.setEquippedTool(player, VyronTool.NONE);
            return true;
        }
        if (!VyronStateManager.isCoreReady(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.tiger_cooldown"), true);
            return true;
        }
        VyronStateManager.setEquippedTool(player, VyronTool.TIGER_CANNON);
        RangedSoundHelper.playThrottled(player.serverLevel(), player.position(), ModSounds.VYRON_TIGER_CANNON_EQUIP.get(),
                SoundSource.PLAYERS, 0.9f, 1.0f, 16.0D, 5, 3.0D);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.tiger_equipped"), true);
        return true;
    }

    private static boolean fireTigerCannon(ServerPlayer player) {
        if (VyronStateManager.equippedTool(player) != VyronTool.TIGER_CANNON) {
            return false;
        }
        if (!VyronStateManager.isCoreReady(player)) {
            player.displayClientMessage(Component.translatable("message.dealt_force_skills.vyron.tiger_cooldown"), true);
            VyronStateManager.setEquippedTool(player, VyronTool.NONE);
            return true;
        }

        ServerLevel level = player.serverLevel();
        VyronTigerCannonEntity shell = new VyronTigerCannonEntity(ModEntities.VYRON_TIGER_CANNON.get(), level, player);
        Vec3 look = player.getLookAngle().normalize();
        Vec3 start = player.getEyePosition().add(look.scale(0.8D));
        shell.setPos(start.x, start.y - 0.08D, start.z);
        shell.setDeltaMovement(look.scale(1.9D).add(0.0D, 0.08D, 0.0D));
        shell.setYRot(player.getYRot());
        shell.setXRot(player.getXRot());
        level.addFreshEntity(shell);
        RangedSoundHelper.playThrottled(level, shell.position(), ModSounds.VYRON_TIGER_CANNON_READY.get(),
                SoundSource.PLAYERS, 0.82f, 1.0f, 18.0D, 6, 3.0D);
        RangedSoundHelper.playThrottled(level, player.position(), ModSounds.VYRON_TIGER_CANNON_FIRE.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f, 18.0D, 4, 3.0D);
        VyronStateManager.setCoreCooldown(player);
        VyronStateManager.grantPowered(player, false);
        VyronStateManager.setEquippedTool(player, VyronTool.NONE);
        return true;
    }
}
