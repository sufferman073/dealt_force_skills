package com.rzy.dealt_force_skills.character.luna;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.CharacterSelectionManager;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.entity.LunaCompositeGrenadeEntity;
import com.rzy.dealt_force_skills.entity.LunaShockArrowEntity;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_LunaRevealEntities;
import com.rzy.dealt_force_skills.network.S2C_SyncLunaState;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillCooldownHelper;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import com.rzy.dealt_force_skills.util.ReconRevealThrottle;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;
import java.util.Optional;

public final class LunaStateManager {
    public static final int SHOCK_ARROW_MAX_CHARGES = 2;
    public static final int SHOCK_ARROW_RECHARGE_TICKS = 30 * 20;
    public static final int COMPOSITE_GRENADE_MAX_CHARGES = 2;
    public static final int COMPOSITE_GRENADE_RECHARGE_TICKS = 30 * 20;
    public static final int COMPOSITE_GRENADE_FUSE_TICKS = 5 * 20;
    public static final int RECON_ARROW_COOLDOWN_TICKS = 45 * 20;
    public static final int MAX_BOW_CHARGE_TICKS = 14;
    public static final int SKILL_REVEAL_TICKS = 30;
    public static final int PASSIVE_REVEAL_TICKS = 20;
    public static final int RECON_REVEAL_TICKS = 40;

    private static final String ROOT_TAG = DealtForceSkillsMod.MODID + ".luna";
    private static final String INITIALIZED = "Initialized";
    private static final String SHOCK_CHARGES = "ShockCharges";
    private static final String SHOCK_NEXT_RECHARGE = "ShockNextRecharge";
    private static final String GRENADE_CHARGES = "GrenadeCharges";
    private static final String GRENADE_NEXT_RECHARGE = "GrenadeNextRecharge";
    private static final String CORE_COOLDOWN_UNTIL = "CoreCooldownUntil";
    private static final String EQUIPPED_TOOL = "EquippedTool";
    private static final String GRENADE_COOK_START = "GrenadeCookStart";
    private static final String SHOCK_BOUNCE_ENABLED = "ShockBounceEnabled";

    private LunaStateManager() {
    }

    public static boolean isLuna(Player player) {
        Optional<String> selected = CharacterSelectionManager.getSelectedCharacterId(player);
        return selected.isPresent() && ModCharacters.LUNA_ID.equals(selected.get());
    }

    public static void initializeIfNeeded(ServerPlayer player) {
        if (!isLuna(player)) {
            return;
        }

        CompoundTag tag = data(player);
        if (tag.getBoolean(INITIALIZED)) {
            return;
        }

        tag.putBoolean(INITIALIZED, true);
        tag.putInt(SHOCK_CHARGES, SHOCK_ARROW_MAX_CHARGES);
        tag.putLong(SHOCK_NEXT_RECHARGE, 0L);
        tag.putInt(GRENADE_CHARGES, COMPOSITE_GRENADE_MAX_CHARGES);
        tag.putLong(GRENADE_NEXT_RECHARGE, 0L);
        tag.putLong(CORE_COOLDOWN_UNTIL, 0L);
        tag.putInt(EQUIPPED_TOOL, LunaTool.NONE.ordinal());
        tag.putLong(GRENADE_COOK_START, 0L);
        tag.putBoolean(SHOCK_BOUNCE_ENABLED, false);
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
        if (!isLuna(player)) {
            return;
        }

        initializeIfNeeded(player);
        long now = player.level().getGameTime();
        recharge(player, now, SHOCK_CHARGES, SHOCK_ARROW_MAX_CHARGES, SHOCK_NEXT_RECHARGE, SHOCK_ARROW_RECHARGE_TICKS);
        recharge(player, now, GRENADE_CHARGES, COMPOSITE_GRENADE_MAX_CHARGES, GRENADE_NEXT_RECHARGE, COMPOSITE_GRENADE_RECHARGE_TICKS);
    }

    public static int shockCharges(Player player) {
        return data(player).getInt(SHOCK_CHARGES);
    }

    public static int shockRechargeRemainingTicks(Player player) {
        if (shockCharges(player) >= SHOCK_ARROW_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, SHOCK_NEXT_RECHARGE);
    }

    public static boolean consumeShockCharge(ServerPlayer player) {
        return consumeCharge(player, SHOCK_CHARGES, SHOCK_ARROW_MAX_CHARGES, SHOCK_NEXT_RECHARGE, SHOCK_ARROW_RECHARGE_TICKS);
    }

    public static int grenadeCharges(Player player) {
        return data(player).getInt(GRENADE_CHARGES);
    }

    public static int grenadeRechargeRemainingTicks(Player player) {
        if (grenadeCharges(player) >= COMPOSITE_GRENADE_MAX_CHARGES) {
            return 0;
        }
        return remainingTicks(player, GRENADE_NEXT_RECHARGE);
    }

    public static boolean consumeGrenadeCharge(ServerPlayer player) {
        return consumeCharge(player, GRENADE_CHARGES, COMPOSITE_GRENADE_MAX_CHARGES,
                GRENADE_NEXT_RECHARGE, COMPOSITE_GRENADE_RECHARGE_TICKS);
    }

    public static LunaTool equippedTool(Player player) {
        int ordinal = data(player).getInt(EQUIPPED_TOOL);
        LunaTool[] values = LunaTool.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : LunaTool.NONE;
    }

    public static void setEquippedTool(Player player, LunaTool tool) {
        data(player).putInt(EQUIPPED_TOOL, tool.ordinal());
        if (tool != LunaTool.COMPOSITE_GRENADE) {
            clearGrenadeCook(player);
        }
    }

    public static boolean shockBounceEnabled(Player player) {
        return data(player).getBoolean(SHOCK_BOUNCE_ENABLED);
    }

    public static void toggleShockBounce(Player player) {
        data(player).putBoolean(SHOCK_BOUNCE_ENABLED, !shockBounceEnabled(player));
    }

    public static void startGrenadeCook(Player player) {
        CompoundTag tag = data(player);
        if (tag.getLong(GRENADE_COOK_START) <= 0L) {
            tag.putLong(GRENADE_COOK_START, player.level().getGameTime());
        }
    }

    public static void clearGrenadeCook(Player player) {
        data(player).putLong(GRENADE_COOK_START, 0L);
    }

    public static int grenadeCookTicks(Player player) {
        long start = data(player).getLong(GRENADE_COOK_START);
        if (start <= 0L || equippedTool(player) != LunaTool.COMPOSITE_GRENADE) {
            return 0;
        }
        long cooked = player.level().getGameTime() - start;
        return cooked > 0L ? (int) Math.min(Integer.MAX_VALUE, cooked) : 0;
    }

    public static boolean isCoreReady(Player player) {
        return player.level().getGameTime() >= data(player).getLong(CORE_COOLDOWN_UNTIL);
    }

    public static int coreCooldownRemainingTicks(Player player) {
        return remainingTicks(player, CORE_COOLDOWN_UNTIL);
    }

    public static void setCoreCooldown(ServerPlayer player) {
        data(player).putLong(CORE_COOLDOWN_UNTIL,
                SkillCooldownHelper.until(player, player.level().getGameTime(), RECON_ARROW_COOLDOWN_TICKS));
    }

    public static void handleDamageReveal(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide || event.getAmount() <= 0.0f) {
            return;
        }

        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer player) || !isLuna(player) || attacker == event.getEntity()) {
            return;
        }

        boolean skillDamage = isLunaSkillDamage(event.getSource());
        revealToOwner(player, event.getEntity(), skillDamage ? SKILL_REVEAL_TICKS : PASSIVE_REVEAL_TICKS, true);
    }

    public static void revealToOwner(ServerPlayer owner, LivingEntity target, int ticks, boolean voiceIfPlayer) {
        if (!isLuna(owner) || target == owner || ticks <= 0) {
            return;
        }
        if (!ReconRevealThrottle.tryStart(target, ticks)) {
            return;
        }

        NetworkHandler.sendToPlayer(new S2C_LunaRevealEntities(List.of(target.getId()), ticks), owner);
        if (target instanceof ServerPlayer) {
            target.level().playSound(null, target.blockPosition(), ModSounds.LUNA_POSITION_REVEAL.get(),
                    SoundSource.PLAYERS, 0.85f, 1.0f);
            if (voiceIfPlayer) {
                owner.level().playSound(null, owner.blockPosition(), ModSounds.LUNA_REVEAL_VOICE.get(),
                        SoundSource.PLAYERS, 0.8f, 1.0f);
            }
        }
    }

    public static void syncToClient(ServerPlayer player) {
        if (!isLuna(player)) {
            return;
        }
        initializeIfNeeded(player);
        NetworkHandler.sendToPlayer(new S2C_SyncLunaState(
                shockCharges(player),
                SHOCK_ARROW_MAX_CHARGES,
                shockRechargeRemainingTicks(player),
                grenadeCharges(player),
                COMPOSITE_GRENADE_MAX_CHARGES,
                grenadeRechargeRemainingTicks(player),
                coreCooldownRemainingTicks(player),
                equippedTool(player).ordinal(),
                grenadeCookTicks(player),
                shockBounceEnabled(player)
        ), player);
    }

    private static boolean isLunaSkillDamage(DamageSource source) {
        if (source.is(SkillDamageHelper.LUNA_SHOCK_ARROW)) {
            return true;
        }
        Entity direct = source.getDirectEntity();
        return direct instanceof LunaShockArrowEntity || direct instanceof LunaCompositeGrenadeEntity;
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

        do {
            charges++;
            if (charges >= maxCharges) {
                tag.putInt(chargesKey, maxCharges);
                tag.putLong(rechargeKey, 0L);
                return;
            }
            nextRecharge += SkillCooldownHelper.ticks(player, rechargeTicks);
        } while (now >= nextRecharge);

        tag.putInt(chargesKey, charges);
        tag.putLong(rechargeKey, nextRecharge);
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
