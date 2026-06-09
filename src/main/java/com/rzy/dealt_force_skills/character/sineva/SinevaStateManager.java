package com.rzy.dealt_force_skills.character.sineva;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.compat.ParcoolStaminaBridge;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SinevaShieldStaminaConsume;
import com.rzy.dealt_force_skills.network.S2C_SyncSinevaRenderState;
import com.rzy.dealt_force_skills.network.S2C_SyncSinevaState;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class SinevaStateManager {
    public static final int BLADE_WIRE_MAX_CHARGES = 2;
    public static final int BLADE_WIRE_RECHARGE_TICKS = 35 * 20;
    public static final int GRAPPLE_COOLDOWN_TICKS = 8 * 20;
    public static final int BOMB_SUIT_COOLDOWN_TICKS = 60 * 20;
    public static final int BOMB_SUIT_EQUIP_TICKS = 30;
    public static final int VIEWPORT_MAX_HEALTH = 250;
    private static final double VIEWPORT_PRESSURE_DECAY_PER_TICK = 0.82D;
    private static final double VIEWPORT_PRESSURE_GAIN_PER_HIT = 8.0D;
    private static final double VIEWPORT_PRESSURE_GAIN_PER_DAMAGE = 0.005D;
    private static final double VIEWPORT_PRESSURE_SCALE = 40.0D;
    private static final double VIEWPORT_PRESSURE_POWER = 2.0D;
    private static final double SHIELD_DAMAGE_SLOW_PER_HIT = 0.04D;
    private static final double SHIELD_DAMAGE_SLOW_MAX = 0.95D;
    private static final int SHIELD_BLOCK_STAMINA_COST_PERCENT = 2;
    private static final int SHIELD_BLOCK_STAMINA_FLOOR_PERCENT = 40;
    private static final int SHIELD_DAMAGE_SLOW_RECOVERY_DELAY_TICKS = 20;
    private static final int SHIELD_DAMAGE_SLOW_RECOVERY_TICKS = 4 * 20;
    /** Keep the heavy suit cadence audible while walking without stacking every tick.
     *  Increased interval + random skip to avoid being too noisy. */
    public static final int BOMB_SUIT_WALK_SOUND_INTERVAL_TICKS = 26;
    public static final java.util.UUID BOMB_SUIT_SLOW_UUID = java.util.UUID.fromString("e52f0e57-bdbf-4d4a-9c8b-7701e62e1b07");

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".sineva";
    private static final String INITIALIZED = "Initialized";
    private static final String BLADE_WIRE_CHARGES = "BladeWireCharges";
    private static final String BLADE_WIRE_NEXT_RECHARGE = "BladeWireNextRecharge";
    private static final String GRAPPLE_COOLDOWN_UNTIL = "GrappleCooldownUntil";
    private static final String BOMB_SUIT_COOLDOWN_UNTIL = "BombSuitCooldownUntil";
    private static final String BOMB_SUIT_EQUIP_UNTIL = "BombSuitEquipUntil";
    private static final String BOMB_SUIT_ACTIVE = "BombSuitActive";
    private static final String SHIELD_DEPLOYED = "ShieldDeployed";
    private static final String VIEWPORT_HEALTH = "ViewportHealth";
    private static final String VIEWPORT_HEALTH_FP = "ViewportHealthFp";
    private static final String VIEWPORT_PRESSURE = "ViewportPressure";
    private static final String VIEWPORT_PRESSURE_TICK = "ViewportPressureTick";
    private static final String SHIELD_DAMAGE_SLOW = "ShieldDamageSlow";
    private static final String SHIELD_DAMAGE_SLOW_TICK = "ShieldDamageSlowTick";
    private static final String BASH_COOLDOWN_UNTIL = "BashCooldownUntil";
    private static final String CHARGE_COOLDOWN_UNTIL = "ChargeCooldownUntil";
    private static final String WALK_SOUND_NEXT_TICK = "WalkSoundNextTick";
    private static final String WALK_SOUND_LAST_X = "WalkSoundLastX";
    private static final String WALK_SOUND_LAST_Z = "WalkSoundLastZ";

    private SinevaStateManager() {
    }

    public static boolean isSineva(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.SINEVA_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isSineva(player)) return;

        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }

        tag.putBoolean(INITIALIZED, true);
        tag.putInt(BLADE_WIRE_CHARGES, BLADE_WIRE_MAX_CHARGES);
        tag.putLong(BLADE_WIRE_NEXT_RECHARGE, 0L);
        tag.putLong(GRAPPLE_COOLDOWN_UNTIL, 0L);
        tag.putLong(BOMB_SUIT_COOLDOWN_UNTIL, 0L);
        tag.putLong(BOMB_SUIT_EQUIP_UNTIL, 0L);
        tag.putBoolean(BOMB_SUIT_ACTIVE, false);
        tag.putBoolean(SHIELD_DEPLOYED, false);
        resetViewport(player);
        tag.putDouble(SHIELD_DAMAGE_SLOW, 0.0D);
        tag.putLong(SHIELD_DAMAGE_SLOW_TICK, 0L);
        tag.putLong(BASH_COOLDOWN_UNTIL, 0L);
        tag.putLong(CHARGE_COOLDOWN_UNTIL, 0L);
        tag.putLong(WALK_SOUND_NEXT_TICK, 0L);
        tag.putDouble(WALK_SOUND_LAST_X, player.getX());
        tag.putDouble(WALK_SOUND_LAST_Z, player.getZ());
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
        if (attr != null && attr.getModifier(BOMB_SUIT_SLOW_UUID) != null) {
            attr.removeModifier(BOMB_SUIT_SLOW_UUID);
        }
    }

    public static void tick(ServerPlayer player) {
        if (!isSineva(player)) return;
        initializeIfNeeded(player);

        long now = player.level().getGameTime();
        rechargeBladeWire(player, now);
        finishBombSuitEquip(player, now);
        playBombSuitWalkSound(player, now);
    }

    public static int bladeWireCharges(Player player) {
        return data(player).getInt(BLADE_WIRE_CHARGES);
    }

    public static int bladeWireRechargeRemainingTicks(Player player) {
        if (bladeWireCharges(player) >= BLADE_WIRE_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, BLADE_WIRE_NEXT_RECHARGE);
    }

    public static boolean consumeBladeWireCharge(ServerPlayer player) {
        CompoundTag tag = data(player);
        int charges = Math.min(BLADE_WIRE_MAX_CHARGES, tag.getInt(BLADE_WIRE_CHARGES));
        if (charges <= 0) {
            return false;
        }

        tag.putInt(BLADE_WIRE_CHARGES, charges - 1);
        if (charges == BLADE_WIRE_MAX_CHARGES) {
            tag.putLong(BLADE_WIRE_NEXT_RECHARGE,
                    SkillCooldownHelper.until(player, player.level().getGameTime(), BLADE_WIRE_RECHARGE_TICKS));
        }
        return true;
    }

    public static boolean isGrappleReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(GRAPPLE_COOLDOWN_UNTIL);
    }

    public static int grappleCooldownRemainingTicks(Player player) {
        return remainingTicks(player, GRAPPLE_COOLDOWN_UNTIL);
    }

    public static void setGrappleCooldown(ServerPlayer player) {
        data(player).putLong(GRAPPLE_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), GRAPPLE_COOLDOWN_TICKS));
    }

    public static boolean isBombSuitCooldownReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(BOMB_SUIT_COOLDOWN_UNTIL);
    }

    public static int bombSuitCooldownRemainingTicks(Player player) {
        return remainingTicks(player, BOMB_SUIT_COOLDOWN_UNTIL);
    }

    public static boolean isBombSuitActive(Player player) {
        return isSineva(player) && data(player).getBoolean(BOMB_SUIT_ACTIVE);
    }

    public static boolean isBombSuitEquipping(Player player) {
        return data(player).getLong(BOMB_SUIT_EQUIP_UNTIL) > player.level().getGameTime();
    }

    public static int bombSuitEquipRemainingTicks(Player player) {
        return remainingTicks(player, BOMB_SUIT_EQUIP_UNTIL);
    }

    public static void startBombSuitEquip(ServerPlayer player) {
        data(player).putLong(BOMB_SUIT_EQUIP_UNTIL, player.level().getGameTime() + BOMB_SUIT_EQUIP_TICKS);
    }

    public static void deactivateBombSuit(ServerPlayer player) {
        CompoundTag tag = data(player);
        tag.putBoolean(BOMB_SUIT_ACTIVE, false);
        tag.putBoolean(SHIELD_DEPLOYED, false);
        tag.putLong(BOMB_SUIT_EQUIP_UNTIL, 0L);
        tag.putLong(BOMB_SUIT_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), BOMB_SUIT_COOLDOWN_TICKS));
    }

    public static boolean isShieldDeployed(Player player) {
        return isBombSuitActive(player) && data(player).getBoolean(SHIELD_DEPLOYED);
    }

    public static void setShieldDeployed(Player player, boolean deployed) {
        data(player).putBoolean(SHIELD_DEPLOYED, deployed);
    }

    public static int viewportHealth(Player player) {
        CompoundTag tag = data(player);
        double health = tag.contains(VIEWPORT_HEALTH_FP)
                ? tag.getDouble(VIEWPORT_HEALTH_FP)
                : tag.getInt(VIEWPORT_HEALTH);
        return (int) Math.ceil(Math.max(0.0D, health));
    }

    public static int calculateViewportMaxHealth(Player player) {
        float armor = (float) player.getAttributeValue(Attributes.ARMOR);
        float reduction = Math.min(0.8f, armor * 0.04f);
        int level = player.experienceLevel;
        float hp = player.getMaxHealth();
        int fullHealth = VIEWPORT_MAX_HEALTH + (int) (hp
                * (1.0f + armor * 0.2f)
                * (1.0f + level)
                * (1.0f + reduction * 5.0f));
        return Math.max(1, fullHealth / 2);
    }

    public static boolean damageViewport(Player player, float amount) {
        CompoundTag tag = data(player);
        double previous = tag.contains(VIEWPORT_HEALTH_FP)
                ? tag.getDouble(VIEWPORT_HEALTH_FP)
                : tag.getInt(VIEWPORT_HEALTH);
        if (previous <= 0.0D || amount <= 0.0F) {
            return false;
        }

        double effectiveDamage = Math.max(0.0D, amount);
        /*
         * Dynamic viewport pressure reduction is intentionally disabled.
         * Restore this block to make repeated viewport hits reduce incoming viewport damage again.
         *
         * double pressure = decayedViewportPressure(player);
         * pressure += VIEWPORT_PRESSURE_GAIN_PER_HIT + amount * VIEWPORT_PRESSURE_GAIN_PER_DAMAGE;
         * double multiplier = 1.0D / Math.pow(1.0D + pressure / VIEWPORT_PRESSURE_SCALE, VIEWPORT_PRESSURE_POWER);
         * effectiveDamage = Math.max(0.0D, amount * multiplier);
         *
         * tag.putDouble(VIEWPORT_PRESSURE, pressure);
         * tag.putLong(VIEWPORT_PRESSURE_TICK, player.level().getGameTime());
         */
        double next = Math.max(0.0D, previous - effectiveDamage);

        tag.putDouble(VIEWPORT_HEALTH_FP, next);
        tag.putInt(VIEWPORT_HEALTH, (int) Math.ceil(next));
        return previous > 0.0D && next <= 0.0D;
    }

    public static void recordShieldDamage(Player player) {
        if (!isShieldDeployed(player)) {
            return;
        }

        CompoundTag tag = data(player);
        long now = player.level().getGameTime();
        double currentSlow = shieldDamageSlow(player, tag, now);
        tag.putDouble(SHIELD_DAMAGE_SLOW, Math.min(SHIELD_DAMAGE_SLOW_MAX, currentSlow + SHIELD_DAMAGE_SLOW_PER_HIT));
        tag.putLong(SHIELD_DAMAGE_SLOW_TICK, now);
    }

    public static void recordPlayerShieldDamage(Player player) {
        if (!isShieldDeployed(player)) {
            return;
        }
        recordShieldDamage(player);
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHandler.sendToPlayer(new S2C_SinevaShieldStaminaConsume(
                    SHIELD_BLOCK_STAMINA_COST_PERCENT,
                    SHIELD_BLOCK_STAMINA_FLOOR_PERCENT
            ), serverPlayer);
        } else if (player.level().isClientSide) {
            ParcoolStaminaBridge.consumeLocalPercentAboveFloor(player,
                    SHIELD_BLOCK_STAMINA_COST_PERCENT,
                    SHIELD_BLOCK_STAMINA_FLOOR_PERCENT);
        }
    }

    public static double shieldDamageSlow(Player player) {
        CompoundTag tag = data(player);
        return shieldDamageSlow(player, tag, player.level().getGameTime());
    }

    public static void clearShieldDamageSlow(Player player) {
        CompoundTag tag = data(player);
        tag.remove(SHIELD_DAMAGE_SLOW);
        tag.remove(SHIELD_DAMAGE_SLOW_TICK);
    }

    public static boolean isBashReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(BASH_COOLDOWN_UNTIL);
    }

    public static int bashCooldownRemainingTicks(Player player) {
        return remainingTicks(player, BASH_COOLDOWN_UNTIL);
    }

    public static void setBashCooldown(ServerPlayer player, int ticks) {
        data(player).putLong(BASH_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), ticks));
    }

    public static boolean isChargeReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(CHARGE_COOLDOWN_UNTIL);
    }

    public static int chargeCooldownRemainingTicks(Player player) {
        return remainingTicks(player, CHARGE_COOLDOWN_UNTIL);
    }

    public static void setChargeCooldown(ServerPlayer player, int ticks) {
        data(player).putLong(CHARGE_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), ticks));
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isSineva(player)) return;
        initializeIfNeeded(player);

        NetworkHandler.sendToPlayer(new S2C_SyncSinevaState(
                bladeWireCharges(player),
                BLADE_WIRE_MAX_CHARGES,
                bladeWireRechargeRemainingTicks(player),
                grappleCooldownRemainingTicks(player),
                bombSuitCooldownRemainingTicks(player),
                bombSuitEquipRemainingTicks(player),
                bashCooldownRemainingTicks(player),
                chargeCooldownRemainingTicks(player),
                isBombSuitActive(player),
                isShieldDeployed(player),
                viewportHealth(player),
                calculateViewportMaxHealth(player)
        ), player);
    }

    public static void syncRenderStateToClients(ServerPlayer player) {
        boolean sineva = isSineva(player);
        NetworkHandler.sendToAll(new S2C_SyncSinevaRenderState(
                player.getId(),
                sineva,
                sineva && isBombSuitActive(player),
                sineva && isShieldDeployed(player),
                sineva && isShieldDeployed(player) && viewportHealth(player) <= 0
        ));
    }

    private static void rechargeBladeWire(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        int charges = Math.min(BLADE_WIRE_MAX_CHARGES, tag.getInt(BLADE_WIRE_CHARGES));
        long nextRecharge = tag.getLong(BLADE_WIRE_NEXT_RECHARGE);

        if (charges >= BLADE_WIRE_MAX_CHARGES || nextRecharge <= 0L || now < nextRecharge) {
            return;
        }

        charges++;
        tag.putInt(BLADE_WIRE_CHARGES, charges);
        tag.putLong(BLADE_WIRE_NEXT_RECHARGE,
                charges < BLADE_WIRE_MAX_CHARGES
                        ? SkillCooldownHelper.until(player, now, BLADE_WIRE_RECHARGE_TICKS)
                        : 0L);
    }

    private static void finishBombSuitEquip(ServerPlayer player, long now) {
        CompoundTag tag = data(player);
        long equipUntil = tag.getLong(BOMB_SUIT_EQUIP_UNTIL);
        if (equipUntil <= 0L || now < equipUntil) {
            return;
        }

        tag.putLong(BOMB_SUIT_EQUIP_UNTIL, 0L);
        tag.putBoolean(BOMB_SUIT_ACTIVE, true);
        tag.putBoolean(SHIELD_DEPLOYED, true);
        resetViewport(player);
        player.level().playSound(null, player.blockPosition(), SoundEvents.ARMOR_EQUIP_NETHERITE,
                SoundSource.PLAYERS, 0.9f, 1.0f);
        player.displayClientMessage(Component.translatable("message.dealt_force_skills.sineva.bomb_suit_ready"), true);
    }

    private static void resetViewport(Player player) {
        CompoundTag tag = data(player);
        int maxHealth = calculateViewportMaxHealth(player);
        tag.putInt(VIEWPORT_HEALTH, maxHealth);
        tag.putDouble(VIEWPORT_HEALTH_FP, maxHealth);
        tag.putDouble(VIEWPORT_PRESSURE, 0.0D);
        tag.putLong(VIEWPORT_PRESSURE_TICK, player.level().getGameTime());
    }

    private static double decayedViewportPressure(Player player) {
        CompoundTag tag = data(player);
        double pressure = Math.max(0.0D, tag.getDouble(VIEWPORT_PRESSURE));
        long lastTick = tag.getLong(VIEWPORT_PRESSURE_TICK);
        long now = player.level().getGameTime();
        long elapsed = Math.max(0L, now - lastTick);
        if (elapsed > 0L && pressure > 0.0D) {
            pressure *= Math.pow(VIEWPORT_PRESSURE_DECAY_PER_TICK, Math.min(200L, elapsed));
        }
        return pressure < 0.001D ? 0.0D : pressure;
    }

    private static double shieldDamageSlow(Player player, CompoundTag tag, long now) {
        double slow = Math.max(0.0D, tag.getDouble(SHIELD_DAMAGE_SLOW));
        if (slow <= 0.0D) {
            return 0.0D;
        }

        long elapsed = Math.max(0L, now - tag.getLong(SHIELD_DAMAGE_SLOW_TICK));
        if (elapsed <= SHIELD_DAMAGE_SLOW_RECOVERY_DELAY_TICKS) {
            return slow;
        }

        long recoveryElapsed = elapsed - SHIELD_DAMAGE_SLOW_RECOVERY_DELAY_TICKS;
        if (recoveryElapsed >= SHIELD_DAMAGE_SLOW_RECOVERY_TICKS) {
            return 0.0D;
        }
        return slow * (SHIELD_DAMAGE_SLOW_RECOVERY_TICKS - recoveryElapsed) / SHIELD_DAMAGE_SLOW_RECOVERY_TICKS;
    }


    private static void playBombSuitWalkSound(ServerPlayer player, long now) {
        if (!isBombSuitActive(player) || player.isSpectator() || !player.onGround()) {
            return;
        }

        CompoundTag tag = data(player);
        double dx = player.getX() - tag.getDouble(WALK_SOUND_LAST_X);
        double dz = player.getZ() - tag.getDouble(WALK_SOUND_LAST_Z);
        tag.putDouble(WALK_SOUND_LAST_X, player.getX());
        tag.putDouble(WALK_SOUND_LAST_Z, player.getZ());

        double horizontalStepSqr = dx * dx + dz * dz;
        if (horizontalStepSqr < 0.0004D && player.getDeltaMovement().horizontalDistanceSqr() < 0.0004D) {
            return;
        }

        long nextWalkSoundTick = tag.getLong(WALK_SOUND_NEXT_TICK);
        if (now < nextWalkSoundTick) {
            return;
        }

        tag.putLong(WALK_SOUND_NEXT_TICK, now + BOMB_SUIT_WALK_SOUND_INTERVAL_TICKS);
        // 35% chance to skip the sound entirely for a less repetitive feel
        if (player.getRandom().nextFloat() < 0.35f) {
            return;
        }
        player.level().playSound(null, player.blockPosition(), ModSounds.SINEVA_WALK.get(),
                SoundSource.PLAYERS, 0.45f, 0.95f + player.getRandom().nextFloat() * 0.1f);
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
