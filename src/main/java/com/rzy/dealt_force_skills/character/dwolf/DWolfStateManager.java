package com.rzy.dealt_force_skills.character.dwolf;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_DWolfStaminaRestore;
import com.rzy.dealt_force_skills.network.S2C_SyncDWolfState;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.UUID;

public final class DWolfStateManager {
    public static final int HAND_CANNON_MAX_CHARGES = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.hand_cannon_max_charges", 2);
    public static final int HAND_CANNON_RECHARGE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.hand_cannon_recharge_ticks", 25 * 20);
    public static final int SMOKE_MAX_CHARGES = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.smoke_max_charges", 2);
    public static final int SMOKE_RECHARGE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.smoke_recharge_ticks", 25 * 20);
    public static final int OVERLOAD_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.overload_cooldown_ticks", 75 * 20);
    public static final int OVERLOAD_STARTUP_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.overload_startup_ticks", 10);
    public static final int OVERLOAD_DURATION_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.overload_duration_ticks", 25 * 20);
    public static final int OVERLOAD_EXTENSION_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.overload_extension_ticks", 5 * 20);
    public static final int OVERLOAD_REGEN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.overload_regen_ticks", 3 * 20);
    public static final int SLIDE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.slide_ticks", 12);
    public static final int SLIDE_STAMINA_PERCENT_COST = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.slide_stamina_percent_cost", 15);
    public static final int SLIDE_COOLDOWN_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.slide_cooldown_ticks", 8);
    public static final UUID OVERLOAD_SPEED_UUID = UUID.fromString("02c58f66-7af1-4a6a-9df8-2b6c5b5224b1");

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".d_wolf";
    private static final String INITIALIZED = "Initialized";
    private static final String HAND_CANNON_CHARGES = "HandCannonCharges";
    private static final String HAND_CANNON_NEXT_RECHARGE = "HandCannonNextRecharge";
    private static final String SMOKE_CHARGES = "SmokeCharges";
    private static final String SMOKE_NEXT_RECHARGE = "SmokeNextRecharge";
    private static final String OVERLOAD_COOLDOWN_UNTIL = "OverloadCooldownUntil";
    private static final String OVERLOAD_STARTUP_UNTIL = "OverloadStartupUntil";
    private static final String OVERLOAD_ACTIVE_UNTIL = "OverloadActiveUntil";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String CANNON_BURST_SHOTS = "CannonBurstShots";
    private static final String CANNON_BURST_NEXT_SHOT = "CannonBurstNextShot";
    private static final String SLIDE_TICKS_REMAINING = "SlideTicksRemaining";
    private static final String SLIDE_COOLDOWN_UNTIL = "SlideCooldownUntil";
    private static final String SLIDE_DIR_X = "SlideDirX";
    private static final String SLIDE_DIR_Z = "SlideDirZ";
    private static final String OVERLOAD_SELF_REWARD_LAST_TICK = "OverloadSelfRewardLastTick";

    private DWolfStateManager() {
    }

    public static boolean isDWolf(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.D_WOLF_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isDWolf(player)) {
            return;
        }

        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }

        tag.putBoolean(INITIALIZED, true);
        tag.putInt(HAND_CANNON_CHARGES, HAND_CANNON_MAX_CHARGES);
        tag.putLong(HAND_CANNON_NEXT_RECHARGE, 0L);
        tag.putInt(SMOKE_CHARGES, SMOKE_MAX_CHARGES);
        tag.putLong(SMOKE_NEXT_RECHARGE, 0L);
        tag.putLong(OVERLOAD_COOLDOWN_UNTIL, 0L);
        tag.putLong(OVERLOAD_STARTUP_UNTIL, 0L);
        tag.putLong(OVERLOAD_ACTIVE_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, DWolfTool.NONE.ordinal());
        tag.putInt(CANNON_BURST_SHOTS, 0);
        tag.putLong(CANNON_BURST_NEXT_SHOT, 0L);
        tag.putInt(SLIDE_TICKS_REMAINING, 0);
        tag.putLong(SLIDE_COOLDOWN_UNTIL, 0L);
        tag.putDouble(SLIDE_DIR_X, 0.0D);
        tag.putDouble(SLIDE_DIR_Z, 0.0D);
        tag.putLong(OVERLOAD_SELF_REWARD_LAST_TICK, Long.MIN_VALUE / 4);
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

    public static void removeTransientModifiers(Player player) {
        var attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null && attr.getModifier(OVERLOAD_SPEED_UUID) != null) {
            attr.removeModifier(OVERLOAD_SPEED_UUID);
        }
    }

    public static void tick(ServerPlayer player) {
        if (!isDWolf(player)) {
            removeTransientModifiers(player);
            return;
        }
        initializeIfNeeded(player);

        long now = player.level().getGameTime();
        recharge(player, now, HAND_CANNON_CHARGES, HAND_CANNON_MAX_CHARGES, HAND_CANNON_NEXT_RECHARGE, HAND_CANNON_RECHARGE_TICKS);
        recharge(player, now, SMOKE_CHARGES, SMOKE_MAX_CHARGES, SMOKE_NEXT_RECHARGE, SMOKE_RECHARGE_TICKS);
        tickCannonBurst(player, now);
        finishOverloadStartup(player, now);
        tickOverload(player, now);
        tickSlide(player);
    }

    public static int handCannonCharges(Player player) {
        return data(player).getInt(HAND_CANNON_CHARGES);
    }

    public static int handCannonRechargeRemainingTicks(Player player) {
        if (handCannonCharges(player) >= HAND_CANNON_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, HAND_CANNON_NEXT_RECHARGE);
    }

    public static boolean consumeHandCannonCharge(ServerPlayer player) {
        return consumeCharge(player, HAND_CANNON_CHARGES, HAND_CANNON_MAX_CHARGES,
                HAND_CANNON_NEXT_RECHARGE, HAND_CANNON_RECHARGE_TICKS);
    }

    public static int smokeCharges(Player player) {
        return data(player).getInt(SMOKE_CHARGES);
    }

    public static int smokeRechargeRemainingTicks(Player player) {
        if (smokeCharges(player) >= SMOKE_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, SMOKE_NEXT_RECHARGE);
    }

    public static boolean consumeSmokeCharge(ServerPlayer player) {
        return consumeCharge(player, SMOKE_CHARGES, SMOKE_MAX_CHARGES, SMOKE_NEXT_RECHARGE, SMOKE_RECHARGE_TICKS);
    }

    public static DWolfTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        DWolfTool[] values = DWolfTool.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : DWolfTool.NONE;
    }

    public static void setEquippedTool(Player player, DWolfTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
    }

    public static int cannonBurstShotsRemaining(Player player) {
        return data(player).getInt(CANNON_BURST_SHOTS);
    }

    public static boolean startCannonBurst(ServerPlayer player) {
        CompoundTag tag = data(player);
        if (tag.getInt(CANNON_BURST_SHOTS) > 0) {
            return true;
        }
        if (!consumeHandCannonCharge(player)) {
            return false;
        }

        long now = player.level().getGameTime();
        tag.putInt(CANNON_BURST_SHOTS, 3);
        tag.putLong(CANNON_BURST_NEXT_SHOT, now);
        return true;
    }

    public static boolean isOverloadReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(OVERLOAD_COOLDOWN_UNTIL);
    }

    public static int overloadCooldownRemainingTicks(Player player) {
        return remainingTicks(player, OVERLOAD_COOLDOWN_UNTIL);
    }

    public static int overloadStartupRemainingTicks(Player player) {
        return remainingTicks(player, OVERLOAD_STARTUP_UNTIL);
    }

    public static boolean isOverloadStarting(Player player) {
        return overloadStartupRemainingTicks(player) > 0;
    }

    public static int overloadActiveRemainingTicks(Player player) {
        return remainingTicks(player, OVERLOAD_ACTIVE_UNTIL);
    }

    public static boolean isOverloadActive(Player player) {
        return isDWolf(player) && overloadActiveRemainingTicks(player) > 0;
    }

    public static void startOverloadStartup(ServerPlayer player) {
        data(player).putLong(OVERLOAD_STARTUP_UNTIL, player.level().getGameTime() + OVERLOAD_STARTUP_TICKS);
    }

    public static void extendOverload(ServerPlayer player) {
        if (!isOverloadActive(player)) {
            return;
        }

        CompoundTag tag = data(player);
        tag.putLong(OVERLOAD_ACTIVE_UNTIL, tag.getLong(OVERLOAD_ACTIVE_UNTIL) + OVERLOAD_EXTENSION_TICKS);
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, OVERLOAD_REGEN_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.effect.regeneration.0.amplifier", 3), false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.SATURATION, OVERLOAD_REGEN_TICKS, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("characters.dwolf.d_wolf_state_manager.effect.saturation.1.amplifier", 1), false, true, true));
        NetworkHandler.sendToPlayer(new S2C_DWolfStaminaRestore(50), player);
        player.level().playSound(null, player.blockPosition(), ModSounds.D_WOLF_OVERLOAD_KILL_EXTENSION.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.d_wolf.overload_extended"), true);
    }

    public static boolean canSelfReward(ServerPlayer player) {
        if (!isOverloadActive(player)) {
            return false;
        }
        long now = player.level().getGameTime();
        long previous = data(player).getLong(OVERLOAD_SELF_REWARD_LAST_TICK);
        return now - previous >= 20L;
    }

    public static void markSelfReward(ServerPlayer player) {
        data(player).putLong(OVERLOAD_SELF_REWARD_LAST_TICK, player.level().getGameTime());
    }

    public static boolean startSlide(ServerPlayer player) {
        if (!isDWolf(player) || !player.onGround() || !player.isSprinting()) {
            return false;
        }

        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        if (tag.getInt(SLIDE_TICKS_REMAINING) > 0 || now < tag.getLong(SLIDE_COOLDOWN_UNTIL)) {
            return false;
        }

        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 0.001D) {
            return false;
        }

        Vec3 direction = horizontal.normalize();
        tag.putInt(SLIDE_TICKS_REMAINING, SLIDE_TICKS);
        tag.putLong(SLIDE_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, SLIDE_COOLDOWN_TICKS));
        tag.putDouble(SLIDE_DIR_X, direction.x);
        tag.putDouble(SLIDE_DIR_Z, direction.z);
        return true;
    }

    public static int slideTicksRemaining(Player player) {
        return data(player).getInt(SLIDE_TICKS_REMAINING);
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isDWolf(player)) {
            return;
        }
        initializeIfNeeded(player);

        NetworkHandler.sendToPlayer(new S2C_SyncDWolfState(
                handCannonCharges(player),
                HAND_CANNON_MAX_CHARGES,
                handCannonRechargeRemainingTicks(player),
                smokeCharges(player),
                SMOKE_MAX_CHARGES,
                smokeRechargeRemainingTicks(player),
                overloadCooldownRemainingTicks(player),
                overloadStartupRemainingTicks(player),
                overloadActiveRemainingTicks(player),
                equippedTool(player).ordinal(),
                cannonBurstShotsRemaining(player),
                slideTicksRemaining(player)
        ), player);
    }

    private static void tickCannonBurst(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        int shots = tag.getInt(CANNON_BURST_SHOTS);
        if (shots <= 0 || now < tag.getLong(CANNON_BURST_NEXT_SHOT)) {
            return;
        }

        DWolfSkills.fireHandCannonGrenade(player);
        shots--;
        tag.putInt(CANNON_BURST_SHOTS, shots);
        tag.putLong(CANNON_BURST_NEXT_SHOT, shots > 0 ? now + 5 : 0L);
        if (shots <= 0) {
            setEquippedTool(player, DWolfTool.NONE);
        }
    }

    private static void finishOverloadStartup(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        long startupUntil = tag.getLong(OVERLOAD_STARTUP_UNTIL);
        if (startupUntil <= 0L || now < startupUntil) {
            return;
        }

        tag.putLong(OVERLOAD_STARTUP_UNTIL, 0L);
        tag.putLong(OVERLOAD_ACTIVE_UNTIL, now + OVERLOAD_DURATION_TICKS);
        NetworkHandler.sendToPlayer(new S2C_DWolfStaminaRestore(50), player);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.d_wolf.overload_active"), true);
        syncToClient(player);
    }

    private static void tickOverload(ServerPlayer player, long now) {
        boolean active = data(player).getLong(OVERLOAD_ACTIVE_UNTIL) > now;
        var attr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (attr != null) {
            AttributeModifier existing = attr.getModifier(OVERLOAD_SPEED_UUID);
            if (active && existing == null) {
                attr.addTransientModifier(new AttributeModifier(
                        OVERLOAD_SPEED_UUID, "d_wolf_overload_speed", 0.5D, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
            if (!active && existing != null) {
                attr.removeModifier(OVERLOAD_SPEED_UUID);
            }
        }

        CompoundTag tag = data(player);
        long activeUntil = tag.getLong(OVERLOAD_ACTIVE_UNTIL);
        if (activeUntil > 0L && now >= activeUntil) {
            tag.putLong(OVERLOAD_ACTIVE_UNTIL, 0L);
            tag.putLong(OVERLOAD_COOLDOWN_UNTIL, SkillCooldownHelper.until(player, now, OVERLOAD_COOLDOWN_TICKS));
            player.level().playSound(null, player.blockPosition(), ModSounds.D_WOLF_OVERLOAD_END.get(),
                    SoundSource.PLAYERS, 1.0f, 1.0f);
            syncToClient(player);
        }
    }

    private static void tickSlide(ServerPlayer player) {
        CompoundTag tag = data(player);
        int ticks = tag.getInt(SLIDE_TICKS_REMAINING);
        if (ticks <= 0) {
            return;
        }

        Vec3 direction = new Vec3(tag.getDouble(SLIDE_DIR_X), 0.0D, tag.getDouble(SLIDE_DIR_Z));
        if (direction.lengthSqr() < 0.001D || player.horizontalCollision) {
            tag.putInt(SLIDE_TICKS_REMAINING, 0);
            return;
        }

        Vec3 motion = direction.normalize().scale(0.9D);
        player.setPose(Pose.SWIMMING);
        player.refreshDimensions();
        player.setDeltaMovement(motion.x, player.getDeltaMovement().y, motion.z);
        player.hurtMarked = true;
        tag.putInt(SLIDE_TICKS_REMAINING, ticks - 1);
    }

    private static boolean consumeCharge(ServerPlayer player, String chargesKey, int maxCharges, String rechargeKey, int rechargeTicks) {
        CompoundTag tag = data(player);
        int charges = Math.min(maxCharges, tag.getInt(chargesKey));
        if (charges <= 0) {
            return false;
        }

        tag.putInt(chargesKey, charges - 1);
        if (charges == maxCharges) {
            tag.putLong(rechargeKey, SkillCooldownHelper.until(player, player.level().getGameTime(), rechargeTicks));
        }
        return true;
    }

    private static void recharge(ServerPlayer player, long now, String chargesKey, int maxCharges, String rechargeKey, int rechargeTicks) {
        CompoundTag tag = data(player);
        int charges = Math.min(maxCharges, tag.getInt(chargesKey));
        long nextRecharge = tag.getLong(rechargeKey);
        if (charges >= maxCharges || nextRecharge <= 0L || now < nextRecharge) {
            return;
        }

        charges++;
        tag.putInt(chargesKey, charges);
        tag.putLong(rechargeKey, charges < maxCharges ? SkillCooldownHelper.until(player, now, rechargeTicks) : 0L);
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
