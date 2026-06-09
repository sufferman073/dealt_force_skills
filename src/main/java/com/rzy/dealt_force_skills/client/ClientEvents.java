package com.rzy.dealt_force_skills.client;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.client.character.ClientCharacterSelectionState;
import com.rzy.dealt_force_skills.client.character.ClientCatDadHudState;
import com.rzy.dealt_force_skills.client.character.ClientDWolfHudState;
import com.rzy.dealt_force_skills.client.character.ClientDepartmentHudState;
import com.rzy.dealt_force_skills.client.character.ClientGizmoHudState;
import com.rzy.dealt_force_skills.client.character.ClientHackclawHudState;
import com.rzy.dealt_force_skills.client.character.ClientLunaHudState;
import com.rzy.dealt_force_skills.client.character.ClientLexNinjiaHudState;
import com.rzy.dealt_force_skills.client.character.ClientManbaHudState;
import com.rzy.dealt_force_skills.client.character.ClientMorseHudState;
import com.rzy.dealt_force_skills.client.character.ClientNikaidouHiroHudState;
import com.rzy.dealt_force_skills.client.character.ClientNoxHudState;
import com.rzy.dealt_force_skills.client.character.ClientRaptorHudState;
import com.rzy.dealt_force_skills.client.character.ClientShepherdHudState;
import com.rzy.dealt_force_skills.client.character.ClientSinevaHudState;
import com.rzy.dealt_force_skills.client.character.ClientSinevaRenderState;
import com.rzy.dealt_force_skills.client.character.ClientStingerHudState;
import com.rzy.dealt_force_skills.client.character.ClientTempestHudState;
import com.rzy.dealt_force_skills.client.character.ClientToxikHudState;
import com.rzy.dealt_force_skills.client.character.ClientUluruHudState;
import com.rzy.dealt_force_skills.client.character.ClientUndeadHudState;
import com.rzy.dealt_force_skills.client.character.ClientVlinderHudState;
import com.rzy.dealt_force_skills.client.character.ClientVyronHudState;
import com.rzy.dealt_force_skills.client.particle.GizmoLargeSmokeParticle;
import com.rzy.dealt_force_skills.client.particle.LargeSmokeParticle;
import com.rzy.dealt_force_skills.client.particle.ToxikLargeSmokeParticle;
import com.rzy.dealt_force_skills.client.renderer.CatDadRoadTruckRenderer;
import com.rzy.dealt_force_skills.client.renderer.NoxDecoyRenderer;
import com.rzy.dealt_force_skills.client.visual.ClientSinevaKnockdownState;
import com.rzy.dealt_force_skills.client.visual.ClientSinevaVisualState;
import com.rzy.dealt_force_skills.client.visual.DfsEquipmentModelVisuals;
import com.rzy.dealt_force_skills.client.visual.DWolfSlideVisuals;
import com.rzy.dealt_force_skills.client.visual.ManbaFlashlightBeamRenderer;
import com.rzy.dealt_force_skills.character.toxik.ToxikStateManager;
import com.rzy.dealt_force_skills.effect.ModItemEffectHelper;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem.Profile;
import com.rzy.dealt_force_skills.item.DfsEquipmentItem.SpecialAbility;
import com.rzy.dealt_force_skills.network.C2S_OpenSelectionOrShop;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModParticles;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleEngine;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT)
public class ClientEvents {
    private static final int CORE_LONG_HOLD_TICKS = 15;
    private static final int SHEPHERD_CORE_LONG_HOLD_TICKS = 15;

    private static int coreKeyHeldTicks = 0;
    private static boolean coreKeyWasDown = false;
    private static int suppressHurtAnimationTicks;
    private static boolean forcedCharacterSpinActive;
    private static float forcedCharacterSpinYaw;
    private static Field minecraftRightClickDelayField;
    private static boolean minecraftRightClickDelayFieldChecked;
    private static final String TACZ_CLIENT_GUN_OPERATOR = "com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator";
    private static final String MHS_FURY_UNTIL = "dealt_force_skills.mhs_fury_until";
    private static final String TACZ_GUN_ID_TAG = "GunId";
    private static final String TRICK_STACKS = "dealt_force_skills.trick_stacks";
    private static final String TRICK_UNTIL = "dealt_force_skills.trick_until";
    private static final String DICH9_STACKS = "dealt_force_skills.dich9_stacks";
    private static final String DICH9_UNTIL = "dealt_force_skills.dich9_until";
    private static final double MASK1_LOCK_RANGE = 64.0D;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onClientTickTaczAdrenaline(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        tickSuppressHurtAnimation(mc.player);
        accelerateTaczAdrenalineClient(mc.player);
        delayTaczMorseShockClient(mc.player);
    }

    public static void suppressLocalHurtAnimation(int ticks) {
        suppressHurtAnimationTicks = Math.max(suppressHurtAnimationTicks, Math.max(1, ticks));
    }

    private static void tickSuppressHurtAnimation(net.minecraft.client.player.LocalPlayer player) {
        if (suppressHurtAnimationTicks <= 0) {
            return;
        }
        player.hurtTime = 0;
        player.hurtDuration = 0;
        suppressHurtAnimationTicks--;
    }

    private static void accelerateTaczAdrenalineClient(net.minecraft.client.player.LocalPlayer player) {
        TaczSpeedMultipliers multipliers = taczSpeedMultipliers(player);
        if (!multipliers.hasChange()) {
            return;
        }
        try {
            Class<?> clientOperatorClass = Class.forName(TACZ_CLIENT_GUN_OPERATOR);
            Method fromLocalPlayer = clientOperatorClass.getMethod("fromLocalPlayer", net.minecraft.client.player.LocalPlayer.class);
            Object operator = fromLocalPlayer.invoke(null, player);
            Object dataHolder = clientOperatorClass.getMethod("getDataHolder").invoke(operator);
            long fireExtraMillis = extraTaczMillis(multipliers.fireRate());
            long reloadExtraMillis = extraTaczMillis(multipliers.reload());
            long aimExtraMillis = extraTaczMillis(multipliers.aim());
            long aimPenaltyMillis = extraTaczMillis(multipliers.aimPenalty());
            shiftLongFieldIfNonNegative(dataHolder, "clientShootTimestamp", -fireExtraMillis);
            shiftLongFieldIfNonNegative(dataHolder, "clientLastShootTimestamp", -fireExtraMillis);
            shiftLongFieldIfNonNegative(dataHolder, "lockTimestamp", -Math.max(fireExtraMillis, reloadExtraMillis));
            if (booleanField(dataHolder, "clientIsAiming")) {
                shiftLongFieldIfNonNegative(dataHolder, "clientAimingTimestamp", -aimExtraMillis + aimPenaltyMillis);
                boostFloatField(dataHolder, "clientAimingProgress", adrenalineAimProgressBoost(multipliers.aim()), 0.0F, 1.0F);
                reduceFloatField(dataHolder, "clientAimingProgress", taczAimPenaltyProgress(multipliers.aimPenalty()), 0.0F, 1.0F);
            }
            if (booleanField(dataHolder, "isCharging")) {
                boostFloatField(dataHolder, "chargeProgress", adrenalineChargeProgressBoost(Math.max(multipliers.fireRate(), multipliers.aim())), 0.0F, Float.MAX_VALUE);
            }
            setBooleanFieldIfPresent(dataHolder, "isShootRecorded", true);
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // TaCZ is optional; keep the bridge best-effort and non-fatal.
        }
    }

    private static float adrenalineAimProgressBoost(double multiplier) {
        return (float) Math.min(0.5D, Math.max(0.0D, 0.08D * (multiplier - 1.0D)));
    }

    private static float adrenalineChargeProgressBoost(double multiplier) {
        return (float) Math.max(0.0D, 0.2D * (multiplier - 1.0D));
    }

    private static TaczSpeedMultipliers taczSpeedMultipliers(net.minecraft.client.player.LocalPlayer player) {
        double adrenaline = ToxikStateManager.adrenalineSpeedMultiplier(player);
        double equipment = equipmentTaczAssaultMultiplier(player);
        return new TaczSpeedMultipliers(
                adrenaline * equipment * ModItemEffectHelper.medicineTaczFireRateMultiplier(player),
                adrenaline * ModItemEffectHelper.medicineTaczReloadMultiplier(player),
                adrenaline * equipment * ModItemEffectHelper.medicineTaczAimSpeedMultiplier(player),
                ModItemEffectHelper.medicineTaczAimPenaltyMultiplier(player)
        );
    }

    private static double equipmentTaczAssaultMultiplier(net.minecraft.client.player.LocalPlayer player) {
        double multiplier = 1.0D;
        long now = player.level().getGameTime();
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        Profile chestProfile = DfsEquipmentItem.profile(chest);
        if (chestProfile != null && chestProfile.ability() == SpecialAbility.TRICK_ASSAULT
                && now <= chest.getOrCreateTag().getLong(TRICK_UNTIL)) {
            multiplier *= 1.0D + chest.getOrCreateTag().getInt(TRICK_STACKS) * 0.04D;
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        Profile headProfile = DfsEquipmentItem.profile(head);
        if (headProfile != null && headProfile.ability() == SpecialAbility.DICH9_ASSAULT
                && now <= head.getOrCreateTag().getLong(DICH9_UNTIL)) {
            multiplier *= 1.0D + head.getOrCreateTag().getInt(DICH9_STACKS) * 0.06D;
        }
        if (isMhsFuryActive(player)) {
            multiplier *= 2.0D;
        }
        return multiplier;
    }

    private static long extraTaczMillis(double multiplier) {
        return multiplier <= 1.0001D ? 0L : Math.max(1L, Math.round(50.0D * (multiplier - 1.0D)));
    }

    private static float taczAimPenaltyProgress(double multiplier) {
        return multiplier <= 1.0001D ? 0.0F : (float) Math.min(0.8D, 0.08D * (multiplier - 1.0D));
    }

    private record TaczSpeedMultipliers(double fireRate, double reload, double aim, double aimPenalty) {
        boolean hasChange() {
            return fireRate > 1.0001D || reload > 1.0001D || aim > 1.0001D || aimPenalty > 1.0001D;
        }
    }

    private static void delayTaczMorseShockClient(net.minecraft.client.player.LocalPlayer player) {
        MobEffectInstance shock = player.getEffect(ModEffects.MORSE_STRONG_SHOCK.get());
        if (shock == null) {
            return;
        }
        long extraMillis = 35L * (shock.getAmplifier() + 1L);
        try {
            Class<?> clientOperatorClass = Class.forName(TACZ_CLIENT_GUN_OPERATOR);
            Method fromLocalPlayer = clientOperatorClass.getMethod("fromLocalPlayer", net.minecraft.client.player.LocalPlayer.class);
            Object operator = fromLocalPlayer.invoke(null, player);
            Object dataHolder = clientOperatorClass.getMethod("getDataHolder").invoke(operator);
            shiftLongFieldIfNonNegative(dataHolder, "clientShootTimestamp", extraMillis);
            shiftLongFieldIfNonNegative(dataHolder, "clientLastShootTimestamp", extraMillis);
            shiftLongFieldIfNonNegative(dataHolder, "lockTimestamp", extraMillis);
            if (booleanField(dataHolder, "clientIsAiming")) {
                shiftLongFieldIfNonNegative(dataHolder, "clientAimingTimestamp", extraMillis);
                reduceFloatField(dataHolder, "clientAimingProgress", 0.04F * (shock.getAmplifier() + 1), 0.0F, 1.0F);
            }
        } catch (ReflectiveOperationException | LinkageError ignored) {
            // TaCZ is optional; strong shock still applies its overlay and server movement penalties.
        }
    }

    private static void shiftLongFieldIfNonNegative(Object target, String fieldName, long delta) throws ReflectiveOperationException {
        if (target == null) {
            return;
        }
        Field field = fieldIfPresent(target, fieldName);
        if (field == null || field.getType() != long.class) {
            return;
        }
        long value = field.getLong(target);
        if (value >= 0L) {
            field.setLong(target, value + delta);
        }
    }

    private static boolean booleanField(Object target, String fieldName) throws ReflectiveOperationException {
        if (target == null) {
            return false;
        }
        Field field = fieldIfPresent(target, fieldName);
        return field != null && field.getType() == boolean.class && field.getBoolean(target);
    }

    private static float floatField(Object target, String fieldName) throws ReflectiveOperationException {
        if (target == null) {
            return 0.0F;
        }
        Field field = fieldIfPresent(target, fieldName);
        return field != null && field.getType() == float.class ? field.getFloat(target) : 0.0F;
    }

    private static void setBooleanFieldIfPresent(Object target, String fieldName, boolean value) throws ReflectiveOperationException {
        if (target == null) {
            return;
        }
        Field field = fieldIfPresent(target, fieldName);
        if (field != null && field.getType() == boolean.class) {
            field.setBoolean(target, value);
        }
    }

    private static void boostFloatField(Object target, String fieldName, float delta, float min, float max) throws ReflectiveOperationException {
        if (target == null || delta <= 0.0F) {
            return;
        }
        Field field = fieldIfPresent(target, fieldName);
        if (field == null || field.getType() != float.class) {
            return;
        }
        float value = field.getFloat(target);
        if (!Float.isFinite(value)) {
            return;
        }
        field.setFloat(target, Math.max(min, Math.min(max, value + delta)));
    }

    private static void reduceFloatField(Object target, String fieldName, float delta, float min, float max) throws ReflectiveOperationException {
        if (target == null || delta <= 0.0F) {
            return;
        }
        Field field = fieldIfPresent(target, fieldName);
        if (field == null || field.getType() != float.class) {
            return;
        }
        float value = field.getFloat(target);
        if (!Float.isFinite(value)) {
            return;
        }
        field.setFloat(target, Math.max(min, Math.min(max, value - delta)));
    }

    private static Field fieldIfPresent(Object target, String fieldName) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException ignored) {
                type = type.getSuperclass();
            }
        }
        return null;
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            forcedCharacterSpinActive = false;
            ClientCharacterSelectionState.resetSession();
            return;
        }

        ClientCharacterSelectionState.openInitialSelectionIfNeeded();
        reduceAdrenalinePlacementDelay(mc);
        tickMask1LockOn(mc);
        tickForcedCharacterSpin(mc.player);
        ClientSinevaHudState.tick();
        ClientUluruHudState.tick();
        ClientDWolfHudState.tick();
        ClientGizmoHudState.tick();
        ClientShepherdHudState.tick();
        ClientLunaHudState.tick();
        ClientHackclawHudState.tick();
        ClientVyronHudState.tick();
        ClientStingerHudState.tick();
        ClientNoxHudState.tick();
        ClientManbaHudState.tick();
        ClientNikaidouHiroHudState.tick();
        ClientCatDadHudState.tick();
        ClientDepartmentHudState.tick();
        ClientUndeadHudState.tick();
        ClientLexNinjiaHudState.tick();
        ClientMorseHudState.tick();
        ClientToxikHudState.tick();
        ClientRaptorHudState.tick();
        ClientVlinderHudState.tick();
        ClientTempestHudState.tick();
        ManbaFlashlightBeamRenderer.tick(mc);
        ClientSinevaRenderState.tick(mc);
        ClientSinevaVisualState.tick(mc);
        ClientSinevaKnockdownState.tick(mc);
        SinevaShieldInputHandler.tick(mc);
        SinevaInputHandler.tick(mc);
        UluruInputHandler.tick(mc);
        DWolfInputHandler.tick(mc);
        GizmoInputHandler.tick(mc);
        ShepherdInputHandler.tick(mc);
        LunaInputHandler.tick(mc);
        HackclawInputHandler.tick(mc);
        VyronInputHandler.tick(mc);
        StingerInputHandler.tick(mc);
        NoxInputHandler.tick(mc);
        ManbaInputHandler.tick(mc);
        NikaidouHiroInputHandler.tick(mc);
        DepartmentInputHandler.tick(mc);
        UndeadInputHandler.tick(mc);
        LexNinjiaInputHandler.tick(mc);
        MorseInputHandler.tick(mc);
        ToxikInputHandler.tick(mc);
        RaptorInputHandler.tick(mc);
        VlinderInputHandler.tick(mc);
        TempestInputHandler.tick(mc);
        UluruMissileController.tick(mc);
        RaptorFalconController.tick(mc);
        UluruGhostEntityManager.tick(mc);
        DWolfSlideVisuals.tick();
        if (mc.player.hasEffect(ModEffects.STINGER_DOWNED.get())
                || mc.player.hasEffect(ModEffects.VLINDER_VITAL_DOWNED.get())
                || mc.player.hasEffect(ModEffects.TEMPEST_EMERGENCY_DOWNED.get())
                || mc.player.hasEffect(ModEffects.TEMPEST_DISARMED.get())
                || ClientCatDadHudState.actionLocked()
                || ClientTempestHudState.recalling()) {
            drainDownedLockedKeys(mc);
            return;
        }
        if (UluruMissileController.isControlling()) {
            drainMissileControlLockedKeys(mc);
            return;
        }
        if (RaptorFalconController.isControlling()) {
            drainMissileControlLockedKeys(mc);
            return;
        }

        // Unified tool-equipped key drain: when any character has a pseudo-tool active,
        // suppress vanilla attack/use/pick to prevent accidental interactions leaking through.
        if (isAnyClientToolActive()) {
            drainKey(mc.options.keyAttack);
            drainKey(mc.options.keyUse);
            drainKey(mc.options.keyPickItem);
            drainKey(mc.options.keyDrop);
            drainKey(mc.options.keySwapOffhand);
        }

        if (!UndeadInputHandler.ownsSelectionKey()) {
            while (KeybindRegister.CHARACTER_SELECT != null && KeybindRegister.CHARACTER_SELECT.consumeClick()) {
                NetworkHandler.sendToServer(new C2S_OpenSelectionOrShop());
            }
        }

        while (!UndeadInputHandler.ownsSkillKeys()
                && KeybindRegister.ACTIVE_SKILL_1 != null
                && KeybindRegister.ACTIVE_SKILL_1.consumeClick()) {
            // Sineva handles ACTIVE_1 exclusively in SinevaInputHandler; other characters use this.
            if (!ClientSinevaHudState.shouldRender() && !ClientStingerHudState.shouldRender()
                    && !ClientNoxHudState.shouldRender() && !ClientManbaHudState.shouldRender()
                    && !ClientHackclawHudState.shouldRender() && !ClientMorseHudState.shouldRender()
                    && !ClientToxikHudState.shouldRender()
                    && !ClientRaptorHudState.shouldRender()
                    && !ClientVlinderHudState.shouldRender()
                    && !ClientTempestHudState.shouldRender()) {
                ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_1);
            }
        }

        while (!UndeadInputHandler.ownsSkillKeys()
                && KeybindRegister.ACTIVE_SKILL_2 != null
                && KeybindRegister.ACTIVE_SKILL_2.consumeClick()) {
            if (!ClientVyronHudState.shouldRender() && !ClientStingerHudState.shouldRender()
                    && !ClientNoxHudState.shouldRender() && !ClientManbaHudState.shouldRender()
                    && !ClientHackclawHudState.shouldRender() && !ClientMorseHudState.shouldRender()
                    && !ClientToxikHudState.shouldRender()
                    && !ClientRaptorHudState.shouldRender()
                    && !ClientVlinderHudState.shouldRender()
                    && !ClientTempestHudState.shouldRender()) {
                ClientCharacterSelectionState.useSkill(SkillSlot.ACTIVE_2);
            }
        }

        if (UndeadInputHandler.ownsSkillKeys()
                || DWolfInputHandler.ownsCoreSkill() || StingerInputHandler.ownsCoreSkill()
                || NoxInputHandler.ownsCoreSkill() || ManbaInputHandler.ownsCoreSkill()
                || MorseInputHandler.ownsCoreSkill()
                || ToxikInputHandler.ownsCoreSkill() || RaptorInputHandler.ownsCoreSkill()
                || VlinderInputHandler.ownsCoreSkill() || TempestInputHandler.ownsCoreSkill()) {
            resetCoreSkillKey();
        } else {
            handleCoreSkillKey();
        }
    }

    private static void tickForcedCharacterSpin(LocalPlayer player) {
        float degrees = 0.0F;
        if (ClientDepartmentHudState.shouldRender()) {
            if (ClientDepartmentHudState.coreCountdownTicks() > 0) {
                degrees = 18.0F;
            } else if (ClientDepartmentHudState.coreAscendTicks() > 0) {
                degrees = 22.0F;
            }
        } else if (ClientUndeadHudState.shouldRender() && ClientUndeadHudState.hunterScatter()) {
            degrees = 18.0F;
        }

        if (degrees <= 0.0F) {
            forcedCharacterSpinActive = false;
            return;
        }
        if (!forcedCharacterSpinActive) {
            forcedCharacterSpinYaw = player.getYRot();
            forcedCharacterSpinActive = true;
        }
        forcedCharacterSpinYaw = Mth.wrapDegrees(forcedCharacterSpinYaw + degrees);
        player.setYHeadRot(forcedCharacterSpinYaw);
        player.setYBodyRot(forcedCharacterSpinYaw);
    }

    private static void tickMask1LockOn(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isSpectator()) {
            return;
        }
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        Profile headProfile = DfsEquipmentItem.profile(head);
        if (headProfile == null || headProfile.ability() != SpecialAbility.MASK_LOCK_ON) {
            return;
        }
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        Profile chestProfile = DfsEquipmentItem.profile(chest);
        if (chestProfile == null || !chestProfile.id().equals("heavy_assault_vest")) {
            return;
        }
        if (!isTaczGunStack(player.getMainHandItem()) || !isTaczClientAiming(player)) {
            return;
        }
        LivingEntity target = nearestMask1LockTarget(player);
        if (target != null) {
            aimLocalPlayerAt(player, target.getEyePosition());
        }
    }

    private static LivingEntity nearestMask1LockTarget(LocalPlayer player) {
        return player.isShiftKeyDown() ? nearestMask1AttackableTarget(player) : nearestMask1PlayerTarget(player);
    }

    private static Player nearestMask1PlayerTarget(LocalPlayer player) {
        Player nearest = null;
        double bestDistance = MASK1_LOCK_RANGE * MASK1_LOCK_RANGE;
        for (Player candidate : player.level().players()) {
            if (candidate == player || !candidate.isAlive() || candidate.isSpectator()) {
                continue;
            }
            double distance = candidate.distanceToSqr(player);
            if (distance < bestDistance) {
                nearest = candidate;
                bestDistance = distance;
            }
        }
        return nearest;
    }

    private static LivingEntity nearestMask1AttackableTarget(LocalPlayer player) {
        LivingEntity nearest = null;
        double bestDistance = MASK1_LOCK_RANGE * MASK1_LOCK_RANGE;
        for (LivingEntity candidate : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(MASK1_LOCK_RANGE),
                entity -> entity != player && entity.isAlive() && !entity.isSpectator()
                        && entity.isAttackable() && entity.attackable())) {
            double distance = candidate.distanceToSqr(player);
            if (distance < bestDistance) {
                nearest = candidate;
                bestDistance = distance;
            }
        }
        return nearest;
    }

    private static void aimLocalPlayerAt(LocalPlayer player, Vec3 target) {
        Vec3 eye = player.getEyePosition();
        double dx = target.x - eye.x;
        double dy = target.y - eye.y;
        double dz = target.z - eye.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        if (horizontal < 0.0001D) {
            return;
        }
        float yaw = (float) (Mth.atan2(dz, dx) * (180F / Math.PI)) - 90.0F;
        float pitch = (float) -(Mth.atan2(dy, horizontal) * (180F / Math.PI));
        float smoothedYaw = Mth.rotLerp(0.65F, player.getYRot(), yaw);
        float smoothedPitch = Mth.lerp(0.65F, player.getXRot(), Mth.clamp(pitch, -89.0F, 89.0F));
        player.setYRot(smoothedYaw);
        player.setXRot(smoothedPitch);
        player.setYHeadRot(smoothedYaw);
        player.setYBodyRot(smoothedYaw);
    }

    private static boolean isTaczClientAiming(LocalPlayer player) {
        try {
            Class<?> clientOperatorClass = Class.forName(TACZ_CLIENT_GUN_OPERATOR);
            Method fromLocalPlayer = clientOperatorClass.getMethod("fromLocalPlayer", LocalPlayer.class);
            Object operator = fromLocalPlayer.invoke(null, player);
            try {
                Object result = clientOperatorClass.getMethod("isAim").invoke(operator);
                if (result instanceof Boolean aiming && aiming) {
                    return true;
                }
            } catch (ReflectiveOperationException ignored) {
                // Older or changed TACZ builds may expose only data-holder fields.
            }
            Object dataHolder = clientOperatorClass.getMethod("getDataHolder").invoke(operator);
            return booleanField(dataHolder, "clientIsAiming")
                    || booleanField(dataHolder, "isAiming")
                    || floatField(dataHolder, "clientAimingProgress") > 0.15F
                    || floatField(dataHolder, "aimingProgress") > 0.15F;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return false;
        }
    }

    private static boolean isMhsFuryActive(LocalPlayer player) {
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        Profile headProfile = DfsEquipmentItem.profile(head);
        CompoundTag tag = head.getTag();
        return headProfile != null && headProfile.ability() == SpecialAbility.MHS_FURY
                && tag != null && player.level().getGameTime() <= tag.getLong(MHS_FURY_UNTIL);
    }

    private static boolean isTaczGunStack(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TACZ_GUN_ID_TAG)) {
            return true;
        }
        return stack.getItem().getClass().getName().startsWith("com.tacz.guns.");
    }


    private static void reduceAdrenalinePlacementDelay(Minecraft minecraft) {
        if (minecraft.player == null || !minecraft.player.hasEffect(ModEffects.TOXIK_ADRENALINE.get())) {
            return;
        }
        Field field = rightClickDelayField();
        if (field == null) {
            return;
        }
        try {
            field.setInt(minecraft, 0);
        } catch (IllegalAccessException ignored) {
        }
    }

    private static Field rightClickDelayField() {
        if (minecraftRightClickDelayFieldChecked) {
            return minecraftRightClickDelayField;
        }
        minecraftRightClickDelayFieldChecked = true;
        for (String name : new String[]{"rightClickDelay", "rightClickDelayTicks", "f_91011_"}) {
            try {
                Field field = Minecraft.class.getDeclaredField(name);
                field.setAccessible(true);
                minecraftRightClickDelayField = field;
                return field;
            } catch (ReflectiveOperationException ignored) {
            }
        }
        return null;
    }

    private static void handleCoreSkillKey() {
        if (KeybindRegister.CORE_SKILL == null) {
            return;
        }

        boolean isDown = KeybindRegister.CORE_SKILL.isDown();
        if (isDown) {
            coreKeyHeldTicks++;
            coreKeyWasDown = true;
            return;
        }

        if (!coreKeyWasDown) {
            return;
        }

        int requiredHoldTicks = ClientShepherdHudState.shouldRender()
                ? SHEPHERD_CORE_LONG_HOLD_TICKS
                : CORE_LONG_HOLD_TICKS;
        boolean longHold = coreKeyHeldTicks >= requiredHoldTicks;
        ClientCharacterSelectionState.useSkill(SkillSlot.CORE, longHold);
        coreKeyHeldTicks = 0;
        coreKeyWasDown = false;
    }

    private static void resetCoreSkillKey() {
        coreKeyHeldTicks = 0;
        coreKeyWasDown = false;
    }

    private static boolean isAnyClientToolActive() {
        // Sineva: shield deployed or blade wire held
        if (ClientSinevaHudState.shouldRender() && ClientSinevaHudState.shieldDeployed()) {
            return true;
        }
        if (SinevaInputHandler.isBladeWireHeld()) {
            return true;
        }
        // Uluru: any tool equipped
        if (ClientUluruHudState.hasEquippedTool()) {
            return true;
        }
        // D-Wolf: hand cannon equipped
        if (ClientDWolfHudState.hasHandCannonEquipped()) {
            return true;
        }
        // Gizmo: any trap/T-boy pseudo-tool equipped
        if (ClientGizmoHudState.hasEquippedTool()) {
            return true;
        }
        // Shepherd: sonic trap or frag grenade pseudo-tool equipped
        if (ClientShepherdHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientLunaHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientHackclawHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientVyronHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientStingerHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientNoxHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientManbaHudState.flashlightActive()) {
            return true;
        }
        if (ClientNikaidouHiroHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientCatDadHudState.actionLocked()) {
            return true;
        }
        if (ClientDepartmentHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientMorseHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientToxikHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientRaptorHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientVlinderHudState.hasEquippedTool()) {
            return true;
        }
        if (ClientTempestHudState.hasEquippedTool()) {
            return true;
        }
        return false;
    }

    private static void drainDownedLockedKeys(Minecraft minecraft) {
        drainKey(KeybindRegister.CHARACTER_SELECT);
        drainKey(KeybindRegister.ACTIVE_SKILL_1);
        drainKey(KeybindRegister.ACTIVE_SKILL_2);
        drainKey(KeybindRegister.CORE_SKILL);
        drainKey(minecraft.options.keyAttack);
        drainKey(minecraft.options.keyUse);
        drainKey(minecraft.options.keyPickItem);
        drainKey(minecraft.options.keyDrop);
        drainKey(minecraft.options.keySwapOffhand);
        drainKey(minecraft.options.keyInventory);
        drainKey(minecraft.options.keyJump);
        coreKeyHeldTicks = 0;
        coreKeyWasDown = false;
    }

    private static void drainMissileControlLockedKeys(Minecraft minecraft) {
        drainKey(KeybindRegister.CHARACTER_SELECT);
        drainKey(KeybindRegister.ACTIVE_SKILL_1);
        drainKey(KeybindRegister.ACTIVE_SKILL_2);
        drainKey(KeybindRegister.CORE_SKILL);
        drainKey(minecraft.options.keyAttack);
        drainKey(minecraft.options.keyUse);
        drainKey(minecraft.options.keyPickItem);
        drainKey(minecraft.options.keyDrop);
        drainKey(minecraft.options.keySwapOffhand);
        drainKey(minecraft.options.keyInventory);
        coreKeyHeldTicks = 0;
        coreKeyWasDown = false;
    }

    private static void drainKey(KeyMapping keyMapping) {
        if (keyMapping == null) {
            return;
        }
        keyMapping.setDown(false);
        while (keyMapping.consumeClick()) {
            // Suppress all gameplay actions while the guided missile owns control.
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        UluruGhostEntityManager.render(event);
    }

    @Mod.EventBusSubscriber(modid = DealtForceSkillsMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModBusClient {
        @SubscribeEvent
        public static void registerParticleProviders(RegisterParticleProvidersEvent e) {
            e.registerSpriteSet(ModParticles.D_WOLF_LARGE_SMOKE.get(),
                    sprites -> (type, level, x, y, z, xs, ys, zs) ->
                            new LargeSmokeParticle(level, x, y, z, xs, ys, zs, sprites));
            e.registerSpriteSet(ModParticles.GIZMO_LARGE_SMOKE.get(),
                    sprites -> (type, level, x, y, z, xs, ys, zs) ->
                            new GizmoLargeSmokeParticle(level, x, y, z, xs, ys, zs, sprites));
            e.registerSpriteSet(ModParticles.TOXIK_LARGE_SMOKE.get(),
                    sprites -> (type, level, x, y, z, xs, ys, zs) ->
                            new ToxikLargeSmokeParticle(level, x, y, z, xs, ys, zs, sprites));
        }

        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
            e.registerEntityRenderer(ModEntities.GRAPPLE_HOOK.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.BLADE_WIRE_PROJECTILE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.ULURU_INCENDIARY_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.ULURU_FIRE_FIELD.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.ULURU_QUICK_COVER_PACKAGE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.ULURU_LOITERING_MISSILE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.ULURU_BOMBLET.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.D_WOLF_HAND_CANNON_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.D_WOLF_SMOKE_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.D_WOLF_SMOKE_CLOUD.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.GIZMO_SMOKE_TRAP.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.GIZMO_SMOKE_CLOUD.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.GIZMO_SPIDER_NEST_TRAP.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.GIZMO_SPIDERLING.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.GIZMO_T_BOY.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.SHEPHERD_SONIC_TRAP.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.SHEPHERD_FRAG_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.SHEPHERD_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.LUNA_SHOCK_ARROW.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.LUNA_COMPOSITE_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.LUNA_RECON_ARROW.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.HACKCLAW_KNIFE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.HACKCLAW_INTERFERENCE_FIELD.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.HACKCLAW_FLASH_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.VYRON_MAGNETIC_BOMB.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.VYRON_TIGER_CANNON.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.STINGER_SMOKE_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.STINGER_SMOKE_CLOUD.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.STINGER_SMOKE_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.STINGER_STIM_PROJECTILE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.NOX_ROTOR_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.NOX_FLASH_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.NOX_DECOY.get(), NoxDecoyRenderer::new);
            e.registerEntityRenderer(ModEntities.MORSE_SHOCK_ORB.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.MORSE_FLASH_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.MORSE_SONAR_DETECTOR.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.TOXIK_TEAR_GAS_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.TOXIK_TEAR_GAS_CLOUD.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.RAPTOR_PULSE_GRENADE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.RAPTOR_FALCON_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.VLINDER_MEDICAL_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.VLINDER_REMOTE_SMOKE_ROUND.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.VLINDER_ACTIVE_DEFENSE_DRONE.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.TEMPEST_WALL_DRILL_STINGER.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.CATDAD_ROAD_TRUCK.get(), CatDadRoadTruckRenderer::new);
            e.registerEntityRenderer(ModEntities.DEPARTMENT_OVERHEAT_LASER.get(), ctx -> new ThrownItemRenderer<>(ctx));
            e.registerEntityRenderer(ModEntities.DEPARTMENT_EXPLOSIVE_TRAP.get(), ctx -> new ThrownItemRenderer<>(ctx));
        }

        @SubscribeEvent
        public static void addLayers(EntityRenderersEvent.AddLayers e) {
            DfsEquipmentModelVisuals.registerLayers(e);
        }
    }
}
