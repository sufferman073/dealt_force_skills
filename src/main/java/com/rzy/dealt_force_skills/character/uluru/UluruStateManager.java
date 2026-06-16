package com.rzy.dealt_force_skills.character.uluru;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_SyncUluruState;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Optional;

public final class UluruStateManager {
    public static final int INCENDIARY_MAX_CHARGES = 2;
    public static final int INCENDIARY_RECHARGE_TICKS = 45 * 20;
    public static final int COVER_MAX_CHARGES = 2;
    public static final int COVER_RECHARGE_TICKS = 30 * 20;
    public static final int MISSILE_COOLDOWN_TICKS = 90 * 20;

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".uluru";
    private static final String INITIALIZED = "Initialized";
    private static final String INCENDIARY_CHARGES = "IncendiaryCharges";
    private static final String INCENDIARY_NEXT_RECHARGE = "IncendiaryNextRecharge";
    private static final String COVER_CHARGES = "CoverCharges";
    private static final String COVER_NEXT_RECHARGE = "CoverNextRecharge";
    private static final String MISSILE_COOLDOWN_UNTIL = "MissileCooldownUntil";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String COVER_PERPENDICULAR = "CoverPerpendicular";

    private UluruStateManager() {
    }

    public static boolean isUluru(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.ULURU_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isUluru(player)) {
            return;
        }

        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }

        tag.putBoolean(INITIALIZED, true);
        tag.putInt(INCENDIARY_CHARGES, INCENDIARY_MAX_CHARGES);
        tag.putLong(INCENDIARY_NEXT_RECHARGE, 0L);
        tag.putInt(COVER_CHARGES, COVER_MAX_CHARGES);
        tag.putLong(COVER_NEXT_RECHARGE, 0L);
        tag.putLong(MISSILE_COOLDOWN_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, UluruTool.NONE.ordinal());
        tag.putBoolean(COVER_PERPENDICULAR, false);
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
        if (!isUluru(player)) {
            return;
        }
        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        recharge(player, now, INCENDIARY_CHARGES, INCENDIARY_MAX_CHARGES, INCENDIARY_NEXT_RECHARGE, INCENDIARY_RECHARGE_TICKS);
        recharge(player, now, COVER_CHARGES, COVER_MAX_CHARGES, COVER_NEXT_RECHARGE, COVER_RECHARGE_TICKS);
    }

    public static int incendiaryCharges(Player player) {
        return data(player).getInt(INCENDIARY_CHARGES);
    }

    public static int incendiaryRechargeRemainingTicks(Player player) {
        if (incendiaryCharges(player) >= INCENDIARY_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, INCENDIARY_NEXT_RECHARGE);
    }

    public static boolean consumeIncendiaryCharge(ServerPlayer player) {
        return consumeCharge(player, INCENDIARY_CHARGES, INCENDIARY_MAX_CHARGES,
                INCENDIARY_NEXT_RECHARGE, INCENDIARY_RECHARGE_TICKS);
    }

    public static int coverCharges(Player player) {
        return data(player).getInt(COVER_CHARGES);
    }

    public static int coverRechargeRemainingTicks(Player player) {
        if (coverCharges(player) >= COVER_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, COVER_NEXT_RECHARGE);
    }

    public static boolean consumeCoverCharge(ServerPlayer player) {
        return consumeCharge(player, COVER_CHARGES, COVER_MAX_CHARGES, COVER_NEXT_RECHARGE, COVER_RECHARGE_TICKS);
    }

    public static boolean isMissileReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(MISSILE_COOLDOWN_UNTIL);
    }

    public static int missileCooldownRemainingTicks(Player player) {
        return remainingTicks(player, MISSILE_COOLDOWN_UNTIL);
    }

    public static void setMissileCooldown(ServerPlayer player) {
        data(player).putLong(MISSILE_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), MISSILE_COOLDOWN_TICKS));
    }

    public static UluruTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        UluruTool[] values = UluruTool.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : UluruTool.NONE;
    }

    public static void setEquippedTool(Player player, UluruTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
    }

    public static boolean coverPerpendicular(Player player) {
        return data(player).getBoolean(COVER_PERPENDICULAR);
    }

    public static void toggleCoverOrientation(Player player) {
        CompoundTag tag = data(player);
        tag.putBoolean(COVER_PERPENDICULAR, !tag.getBoolean(COVER_PERPENDICULAR));
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isUluru(player)) {
            return;
        }
        initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_SyncUluruState(
                incendiaryCharges(player),
                INCENDIARY_MAX_CHARGES,
                incendiaryRechargeRemainingTicks(player),
                coverCharges(player),
                COVER_MAX_CHARGES,
                coverRechargeRemainingTicks(player),
                missileCooldownRemainingTicks(player),
                equippedTool(player).ordinal(),
                coverPerpendicular(player)
        ), player);
    }

    private static boolean consumeCharge(
            ServerPlayer player,
            String chargesKey,
            int maxCharges,
            String rechargeKey,
            int rechargeTicks
    ) {
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
