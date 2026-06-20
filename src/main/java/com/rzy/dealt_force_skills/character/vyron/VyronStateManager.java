package com.rzy.dealt_force_skills.character.vyron;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncVyronState;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public final class VyronStateManager {
    public static final int DASH_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.dash_cooldown_ticks", 8 * 20);
    public static final int DASH_DURATION_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.dash_duration_ticks", 12);
    public static final int POWERED_DURATION_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.powered_duration_ticks", 3 * 20);
    public static final int MAGNETIC_BOMB_MAX_CHARGES = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.magnetic_bomb_max_charges", 2);
    public static final int MAGNETIC_BOMB_RECHARGE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.magnetic_bomb_recharge_ticks", 28 * 20);
    public static final int MAGNETIC_BOMB_FUSE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.magnetic_bomb_fuse_ticks", 3 * 20);
    public static final int TIGER_CANNON_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.tiger_cannon_cooldown_ticks", 45 * 20);
    public static final UUID POWERED_SPEED_UUID = UUID.fromString("f98e31d3-cd09-4e75-94dc-0180ad55d97a");

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".vyron";
    private static final String INITIALIZED = "Initialized";
    private static final String DASH_COOLDOWN_UNTIL = "DashCooldownUntil";
    private static final String DASH_TICKS = "DashTicks";
    private static final String DASH_DIR_X = "DashDirX";
    private static final String DASH_DIR_Z = "DashDirZ";
    private static final String POWERED_UNTIL = "PoweredUntil";
    private static final String BOMB_CHARGES = "BombCharges";
    private static final String BOMB_NEXT_RECHARGE = "BombNextRecharge";
    private static final String CORE_COOLDOWN_UNTIL = "CoreCooldownUntil";
    private static final String EQUIPPED_TOOL = "EquippedTool";

    private VyronStateManager() {
    }

    public static boolean isVyron(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.VYRON_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isVyron(player)) {
            return;
        }

        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }

        tag.putBoolean(INITIALIZED, true);
        tag.putLong(DASH_COOLDOWN_UNTIL, 0L);
        tag.putInt(DASH_TICKS, 0);
        tag.putDouble(DASH_DIR_X, 0.0D);
        tag.putDouble(DASH_DIR_Z, 0.0D);
        tag.putLong(POWERED_UNTIL, 0L);
        tag.putInt(BOMB_CHARGES, MAGNETIC_BOMB_MAX_CHARGES);
        tag.putLong(BOMB_NEXT_RECHARGE, 0L);
        tag.putLong(CORE_COOLDOWN_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, VyronTool.NONE.ordinal());
    }

    public static void copyState(Player original, Player target) {
        CompoundTag originalData = original.getPersistentData();
        if (originalData.contains(ROOT_TAG, Tag.TAG_COMPOUND)) {
            target.getPersistentData().put(ROOT_TAG, originalData.getCompound(ROOT_TAG).copy());
        }
    }

    public static void clearState(Player player) {
        player.getPersistentData().remove(ROOT_TAG);
        removePoweredModifier(player);
    }

    public static void tick(ServerPlayer player) {
        if (!isVyron(player)) {
            removePoweredModifier(player);
            return;
        }

        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        recharge(player, now);
        tickDash(player);
        tickPowered(player, now);
    }

    public static boolean startDash(ServerPlayer player, Vec3 direction) {
        if (!isVyron(player) || !isDashReady(player)) {
            return false;
        }
        Vec3 horizontal = new Vec3(direction.x, 0.0D, direction.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            Vec3 look = player.getLookAngle();
            horizontal = new Vec3(look.x, 0.0D, look.z);
        }
        if (horizontal.lengthSqr() < 0.0001D) {
            return false;
        }

        CompoundTag tag = data(player);
        Vec3 normalized = horizontal.normalize();
        long now = player.level().getGameTime();
        tag.putInt(DASH_TICKS, DASH_DURATION_TICKS);
        tag.putDouble(DASH_DIR_X, normalized.x);
        tag.putDouble(DASH_DIR_Z, normalized.z);
        tag.putLong(DASH_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, DASH_COOLDOWN_TICKS));
        grantPowered(player, false);
        player.level().playSound(null, player.blockPosition(), ModSounds.VYRON_DASH.get(), SoundSource.PLAYERS, 1.0f, 1.0f);
        return true;
    }

    public static boolean isDashReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(DASH_COOLDOWN_UNTIL);
    }

    public static int dashCooldownRemainingTicks(Player player) {
        return remainingTicks(player, DASH_COOLDOWN_UNTIL);
    }

    public static int dashTicks(Player player) {
        return data(player).getInt(DASH_TICKS);
    }

    public static void resetDashCooldown(ServerPlayer player) {
        if (!isVyron(player)) {
            return;
        }
        data(player).putLong(DASH_COOLDOWN_UNTIL, 0L);
        syncToClient(player);
    }

    public static void grantPowered(ServerPlayer player, boolean landing) {
        if (!isVyron(player)) {
            return;
        }
        data(player).putLong(POWERED_UNTIL, player.level().getGameTime() + POWERED_DURATION_TICKS);
        player.addEffect(new MobEffectInstance(ModEffects.VYRON_POWERED.get(), POWERED_DURATION_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.vyron.vyron_state_manager.effect.vyron_powered.0.amplifier", 0), false, true, true));
        player.level().playSound(null, player.blockPosition(), landing ? ModSounds.VYRON_POWERED_LAND.get() : ModSounds.VYRON_POWERED_ACTIVATE.get(),
                SoundSource.PLAYERS, 0.85f, 1.0f);
    }

    public static int poweredRemainingTicks(Player player) {
        return remainingTicks(player, POWERED_UNTIL);
    }

    public static boolean isPowered(Player player) {
        return isVyron(player) && poweredRemainingTicks(player) > 0;
    }

    public static int bombCharges(Player player) {
        return data(player).getInt(BOMB_CHARGES);
    }

    public static int bombRechargeRemainingTicks(Player player) {
        if (bombCharges(player) >= MAGNETIC_BOMB_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, BOMB_NEXT_RECHARGE);
    }

    public static boolean consumeBombCharge(ServerPlayer player) {
        CompoundTag tag = data(player);
        int charges = Math.min(MAGNETIC_BOMB_MAX_CHARGES, tag.getInt(BOMB_CHARGES));
        if (charges <= 0) {
            return false;
        }
        tag.putInt(BOMB_CHARGES, charges - 1);
        if (charges == MAGNETIC_BOMB_MAX_CHARGES) {
            tag.putLong(BOMB_NEXT_RECHARGE,
                    SkillCooldownHelper.until(player, player.level().getGameTime(), MAGNETIC_BOMB_RECHARGE_TICKS));
        }
        return true;
    }

    public static VyronTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        VyronTool[] values = VyronTool.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : VyronTool.NONE;
    }

    public static void setEquippedTool(Player player, VyronTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
    }

    public static boolean isCoreReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(CORE_COOLDOWN_UNTIL);
    }

    public static int coreCooldownRemainingTicks(Player player) {
        return remainingTicks(player, CORE_COOLDOWN_UNTIL);
    }

    public static void setCoreCooldown(ServerPlayer player) {
        data(player).putLong(CORE_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), TIGER_CANNON_COOLDOWN_TICKS));
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isVyron(player)) {
            return;
        }
        initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_SyncVyronState(
                dashCooldownRemainingTicks(player),
                dashTicks(player),
                poweredRemainingTicks(player),
                bombCharges(player),
                MAGNETIC_BOMB_MAX_CHARGES,
                bombRechargeRemainingTicks(player),
                coreCooldownRemainingTicks(player),
                equippedTool(player).ordinal()
        ), player);
    }

    private static void tickDash(ServerPlayer player) {
        CompoundTag tag = data(player);
        int ticks = tag.getInt(DASH_TICKS);
        if (ticks <= 0) {
            return;
        }

        Vec3 direction = new Vec3(tag.getDouble(DASH_DIR_X), 0.0D, tag.getDouble(DASH_DIR_Z));
        if (direction.lengthSqr() < 0.0001D || player.horizontalCollision) {
            tag.putInt(DASH_TICKS, 0);
            return;
        }

        Vec3 motion = direction.normalize().scale(2.05D);
        player.setDeltaMovement(motion.x, 0.0D, motion.z);
        player.fallDistance = 0.0F;
        player.hurtMarked = true;
        tag.putInt(DASH_TICKS, ticks - 1);
    }

    private static void tickPowered(ServerPlayer player, long now) {
        boolean active = data(player).getLong(POWERED_UNTIL) > now;
        var attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr == null) {
            return;
        }
        AttributeModifier existing = attr.getModifier(POWERED_SPEED_UUID);
        if (active && existing == null) {
            attr.addTransientModifier(new AttributeModifier(
                    POWERED_SPEED_UUID, "vyron_powered_speed", 0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
        if (!active && existing != null) {
            attr.removeModifier(POWERED_SPEED_UUID);
        }
    }

    private static void removePoweredModifier(Player player) {
        var attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && attr.getModifier(POWERED_SPEED_UUID) != null) {
            attr.removeModifier(POWERED_SPEED_UUID);
        }
        player.removeEffect(ModEffects.VYRON_POWERED.get());
    }

    private static void recharge(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        int charges = Math.min(MAGNETIC_BOMB_MAX_CHARGES, tag.getInt(BOMB_CHARGES));
        long nextRecharge = tag.getLong(BOMB_NEXT_RECHARGE);
        if (charges >= MAGNETIC_BOMB_MAX_CHARGES || nextRecharge <= 0L || now < nextRecharge) {
            return;
        }

        do {
            charges++;
            if (charges >= MAGNETIC_BOMB_MAX_CHARGES) {
                tag.putInt(BOMB_CHARGES, MAGNETIC_BOMB_MAX_CHARGES);
                tag.putLong(BOMB_NEXT_RECHARGE, 0L);
                return;
            }
            nextRecharge += SkillCooldownHelper.ticks(player, MAGNETIC_BOMB_RECHARGE_TICKS);
        } while (now >= nextRecharge);

        tag.putInt(BOMB_CHARGES, charges);
        tag.putLong(BOMB_NEXT_RECHARGE, nextRecharge);
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
