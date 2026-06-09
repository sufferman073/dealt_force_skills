package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.effect.CatDadArmorReducedEffect;
import com.rzy.dealt_force_skills.effect.CatDadDownedEffect;
import com.rzy.dealt_force_skills.effect.CatDadHissSlowEffect;
import com.rzy.dealt_force_skills.effect.CarryBoostEffect;
import com.rzy.dealt_force_skills.effect.CharacterFrameworkEffect;
import com.rzy.dealt_force_skills.effect.ContinuousHealingEffect;
import com.rzy.dealt_force_skills.effect.CorrosionEffect;
import com.rzy.dealt_force_skills.effect.FatigueRemovalEffect;
import com.rzy.dealt_force_skills.effect.HackclawFlashBlindEffect;
import com.rzy.dealt_force_skills.effect.HelaEffect;
import com.rzy.dealt_force_skills.effect.ItemWeaknessEffect;
import com.rzy.dealt_force_skills.effect.LaughingManiaOneEffect;
import com.rzy.dealt_force_skills.effect.LaughingManiaThreeEffect;
import com.rzy.dealt_force_skills.effect.LaughingManiaTwoEffect;
import com.rzy.dealt_force_skills.effect.ManbaBlindedEffect;
import com.rzy.dealt_force_skills.effect.MorseFlashedEffect;
import com.rzy.dealt_force_skills.effect.MorseStrongShockEffect;
import com.rzy.dealt_force_skills.effect.NikaidouCoreEffect;
import com.rzy.dealt_force_skills.effect.NikaidouCorrectionEffect;
import com.rzy.dealt_force_skills.effect.NikaidouDoomedEffect;
import com.rzy.dealt_force_skills.effect.NikaidouRiftStacksEffect;
import com.rzy.dealt_force_skills.effect.NoxCrippledEffect;
import com.rzy.dealt_force_skills.effect.NoxDelayedWoundEffect;
import com.rzy.dealt_force_skills.effect.NoxFlashedEffect;
import com.rzy.dealt_force_skills.effect.NoxStealthEffect;
import com.rzy.dealt_force_skills.effect.PainReliefEffect;
import com.rzy.dealt_force_skills.effect.PlannedStatusEffect;
import com.rzy.dealt_force_skills.effect.RaptorActionPauseEffect;
import com.rzy.dealt_force_skills.effect.RaptorElectromagneticInterferenceEffect;
import com.rzy.dealt_force_skills.effect.RaptorHummingbirdMarkedEffect;
import com.rzy.dealt_force_skills.effect.SonicShockEffect;
import com.rzy.dealt_force_skills.effect.StaminaBoostEffect;
import com.rzy.dealt_force_skills.effect.StaminaCapacityEffect;
import com.rzy.dealt_force_skills.effect.StingerDownedEffect;
import com.rzy.dealt_force_skills.effect.StingerSmokeRegenEffect;
import com.rzy.dealt_force_skills.effect.StingerStimHealEffect;
import com.rzy.dealt_force_skills.effect.StingerStimSuppressionEffect;
import com.rzy.dealt_force_skills.effect.StunEffect;
import com.rzy.dealt_force_skills.effect.TempestDisarmedEffect;
import com.rzy.dealt_force_skills.effect.TempestEmergencyDownedEffect;
import com.rzy.dealt_force_skills.effect.TempestExplosiveSpineEffect;
import com.rzy.dealt_force_skills.effect.ToxikAdrenalineEffect;
import com.rzy.dealt_force_skills.effect.ToxikFireflyInterferenceEffect;
import com.rzy.dealt_force_skills.effect.ToxikTearGasBlindEffect;
import com.rzy.dealt_force_skills.effect.VlinderHealingDustEffect;
import com.rzy.dealt_force_skills.effect.VlinderMedicalWasteInterferenceEffect;
import com.rzy.dealt_force_skills.effect.VlinderPlasmaInjectedEffect;
import com.rzy.dealt_force_skills.effect.VlinderRescueProtectionEffect;
import com.rzy.dealt_force_skills.effect.VlinderVitalDownedEffect;
import com.rzy.dealt_force_skills.effect.VyronPoweredEffect;
import com.rzy.dealt_force_skills.effect.WebbedEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, DealtForceSkillsMod.MODID);

    public static final RegistryObject<MobEffect> STUN =
            EFFECTS.register("stun", StunEffect::new);

    public static final RegistryObject<MobEffect> CHARACTER_FRAMEWORK =
            EFFECTS.register("character_framework", CharacterFrameworkEffect::new);

    public static final RegistryObject<MobEffect> CORROSION =
            EFFECTS.register("corrosion", CorrosionEffect::new);

    public static final RegistryObject<MobEffect> WEBBED =
            EFFECTS.register("webbed", WebbedEffect::new);

    public static final RegistryObject<MobEffect> SONIC_SHOCK =
            EFFECTS.register("sonic_shock", SonicShockEffect::new);

    public static final RegistryObject<MobEffect> VYRON_POWERED =
            EFFECTS.register("vyron_powered", VyronPoweredEffect::new);

    public static final RegistryObject<MobEffect> STINGER_DOWNED =
            EFFECTS.register("stinger_downed", StingerDownedEffect::new);

    public static final RegistryObject<MobEffect> STINGER_STIM_HEAL =
            EFFECTS.register("stinger_stim_heal", StingerStimHealEffect::new);

    public static final RegistryObject<MobEffect> STINGER_STIM_SUPPRESSION =
            EFFECTS.register("stinger_stim_suppression", StingerStimSuppressionEffect::new);

    public static final RegistryObject<MobEffect> STINGER_SMOKE_REGEN =
            EFFECTS.register("stinger_smoke_regen", StingerSmokeRegenEffect::new);

    public static final RegistryObject<MobEffect> NOX_DELAYED_WOUND =
            EFFECTS.register("nox_delayed_wound", NoxDelayedWoundEffect::new);

    public static final RegistryObject<MobEffect> NOX_CRIPPLED =
            EFFECTS.register("nox_crippled", NoxCrippledEffect::new);

    public static final RegistryObject<MobEffect> NOX_FLASHED =
            EFFECTS.register("nox_flashed", NoxFlashedEffect::new);

    public static final RegistryObject<MobEffect> NOX_STEALTH =
            EFFECTS.register("nox_stealth", NoxStealthEffect::new);

    public static final RegistryObject<MobEffect> MANBA_BLINDED =
            EFFECTS.register("manba_blinded", ManbaBlindedEffect::new);

    public static final RegistryObject<MobEffect> PAIN_RELIEF =
            EFFECTS.register("pain_relief", PainReliefEffect::new);
    public static final RegistryObject<MobEffect> ITEM_WEAKNESS =
            EFFECTS.register("item_weakness", ItemWeaknessEffect::new);
    public static final RegistryObject<MobEffect> HELA =
            EFFECTS.register("hela", HelaEffect::new);
    public static final RegistryObject<MobEffect> LAUGHING_MANIA_I =
            EFFECTS.register("laughing_mania_i", LaughingManiaOneEffect::new);
    public static final RegistryObject<MobEffect> LAUGHING_MANIA_II =
            EFFECTS.register("laughing_mania_ii", LaughingManiaTwoEffect::new);
    public static final RegistryObject<MobEffect> LAUGHING_MANIA_III =
            EFFECTS.register("laughing_mania_iii", LaughingManiaThreeEffect::new);
    public static final RegistryObject<MobEffect> STAMINA_BOOST =
            EFFECTS.register("stamina_boost", StaminaBoostEffect::new);
    public static final RegistryObject<MobEffect> CARRY_BOOST =
            EFFECTS.register("carry_boost", CarryBoostEffect::new);
    public static final RegistryObject<MobEffect> HEARING_AMPLIFICATION =
            EFFECTS.register("hearing_amplification", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x9DD7FF));
    public static final RegistryObject<MobEffect> STAMINA_CAPACITY =
            EFFECTS.register("stamina_capacity", StaminaCapacityEffect::new);
    public static final RegistryObject<MobEffect> FATIGUE_REMOVAL =
            EFFECTS.register("fatigue_removal", FatigueRemovalEffect::new);
    public static final RegistryObject<MobEffect> CONTINUOUS_HEALING =
            EFFECTS.register("continuous_healing", ContinuousHealingEffect::new);
    public static final RegistryObject<MobEffect> SEDATION =
            EFFECTS.register("sedation", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x8CB4FF));
    public static final RegistryObject<MobEffect> SMALL_INTERACTION_SPEED =
            EFFECTS.register("small_interaction_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x79D46A));
    public static final RegistryObject<MobEffect> LARGE_INTERACTION_SPEED =
            EFFECTS.register("large_interaction_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x3DE76F));
    public static final RegistryObject<MobEffect> SMALL_ITEM_USE_SPEED =
            EFFECTS.register("small_item_use_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x79D46A));
    public static final RegistryObject<MobEffect> LARGE_ITEM_USE_SPEED =
            EFFECTS.register("large_item_use_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x3DE76F));
    public static final RegistryObject<MobEffect> MEDIUM_RELOAD_SPEED =
            EFFECTS.register("medium_reload_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x58C879));
    public static final RegistryObject<MobEffect> LARGE_RELOAD_SPEED =
            EFFECTS.register("large_reload_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x3DE76F));
    public static final RegistryObject<MobEffect> SMALL_AIM_SPEED =
            EFFECTS.register("small_aim_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x79D46A));
    public static final RegistryObject<MobEffect> LARGE_AIM_SPEED =
            EFFECTS.register("large_aim_speed", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x3DE76F));
    public static final RegistryObject<MobEffect> LARGE_FIRE_RATE =
            EFFECTS.register("large_fire_rate", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x3DE76F));
    public static final RegistryObject<MobEffect> LARGE_AIM_PENALTY =
            EFFECTS.register("large_aim_penalty", () -> new PlannedStatusEffect(MobEffectCategory.HARMFUL, 0xB84C4C));
    // Planned status/effect icon hooks reserved from upcoming character designs.

    public static final RegistryObject<MobEffect> HACKCLAW_INTERFERENCE =
            EFFECTS.register("hackclaw_interference", () -> new PlannedStatusEffect(MobEffectCategory.NEUTRAL, 0x32C8FF));
    public static final RegistryObject<MobEffect> HACKCLAW_FLASH_BLIND =
            EFFECTS.register("hackclaw_flash_blind", HackclawFlashBlindEffect::new);
    public static final RegistryObject<MobEffect> MORSE_STRONG_SHOCK =
            EFFECTS.register("morse_strong_shock", MorseStrongShockEffect::new);
    public static final RegistryObject<MobEffect> MORSE_FLASH_BLIND =
            EFFECTS.register("morse_flash_blind", MorseFlashedEffect::new);
    public static final RegistryObject<MobEffect> MORSE_SONAR_REVEALED =
            EFFECTS.register("morse_sonar_revealed", () -> new PlannedStatusEffect(MobEffectCategory.NEUTRAL, 0x48B8FF));
    public static final RegistryObject<MobEffect> RAPTOR_ACTION_PAUSE =
            EFFECTS.register("raptor_action_pause", RaptorActionPauseEffect::new);
    public static final RegistryObject<MobEffect> RAPTOR_ELECTROMAGNETIC_INTERFERENCE =
            EFFECTS.register("raptor_electromagnetic_interference", RaptorElectromagneticInterferenceEffect::new);
    public static final RegistryObject<MobEffect> RAPTOR_HUMMINGBIRD_MARKED =
            EFFECTS.register("raptor_hummingbird_marked", RaptorHummingbirdMarkedEffect::new);
    public static final RegistryObject<MobEffect> TEMPEST_EXPLOSIVE_SPINE =
            EFFECTS.register("tempest_explosive_spine", TempestExplosiveSpineEffect::new);
    public static final RegistryObject<MobEffect> TEMPEST_DISARMED =
            EFFECTS.register("tempest_disarmed", TempestDisarmedEffect::new);
    public static final RegistryObject<MobEffect> TEMPEST_EMERGENCY_DOWNED =
            EFFECTS.register("tempest_emergency_downed", TempestEmergencyDownedEffect::new);
    public static final RegistryObject<MobEffect> TOXIK_ADRENALINE =
            EFFECTS.register("toxik_adrenaline", ToxikAdrenalineEffect::new);
    public static final RegistryObject<MobEffect> TOXIK_TEAR_GAS_BLIND =
            EFFECTS.register("toxik_tear_gas_blind", ToxikTearGasBlindEffect::new);
    public static final RegistryObject<MobEffect> TOXIK_FIREFLY_INTERFERENCE =
            EFFECTS.register("toxik_firefly_interference", ToxikFireflyInterferenceEffect::new);
    public static final RegistryObject<MobEffect> VLINDER_HEALING_DUST =
            EFFECTS.register("vlinder_healing_dust", VlinderHealingDustEffect::new);
    public static final RegistryObject<MobEffect> VLINDER_MEDICAL_WASTE_INTERFERENCE =
            EFFECTS.register("vlinder_medical_waste_interference", VlinderMedicalWasteInterferenceEffect::new);
    public static final RegistryObject<MobEffect> VLINDER_VITAL_DOWNED =
            EFFECTS.register("vlinder_vital_downed", VlinderVitalDownedEffect::new);
    public static final RegistryObject<MobEffect> VLINDER_RESCUE_PROTECTION =
            EFFECTS.register("vlinder_rescue_protection", VlinderRescueProtectionEffect::new);
    public static final RegistryObject<MobEffect> VLINDER_PLASMA_INJECTED =
            EFFECTS.register("vlinder_plasma_injected", VlinderPlasmaInjectedEffect::new);
    public static final RegistryObject<MobEffect> NIKAIDOU_RIFT_STACKS =
            EFFECTS.register("nikaidou_rift_stacks", NikaidouRiftStacksEffect::new);
    public static final RegistryObject<MobEffect> NIKAIDOU_CORRECTION =
            EFFECTS.register("nikaidou_correction", NikaidouCorrectionEffect::new);
    public static final RegistryObject<MobEffect> NIKAIDOU_CORE =
            EFFECTS.register("nikaidou_core", NikaidouCoreEffect::new);
    public static final RegistryObject<MobEffect> NIKAIDOU_DOOMED =
            EFFECTS.register("nikaidou_doomed", NikaidouDoomedEffect::new);
    public static final RegistryObject<MobEffect> CATDAD_HISS_SLOW =
            EFFECTS.register("catdad_hiss_slow", CatDadHissSlowEffect::new);
    public static final RegistryObject<MobEffect> CATDAD_ARMOR_REDUCED =
            EFFECTS.register("catdad_armor_reduced", CatDadArmorReducedEffect::new);
    public static final RegistryObject<MobEffect> CATDAD_DOWNED =
            EFFECTS.register("catdad_downed", CatDadDownedEffect::new);
    public static final RegistryObject<MobEffect> DEPARTMENT_CALIBRATION =
            EFFECTS.register("department_calibration", () -> new PlannedStatusEffect(MobEffectCategory.HARMFUL, 0xFFD84A));
    public static final RegistryObject<MobEffect> DEPARTMENT_VULNERABLE =
            EFFECTS.register("department_vulnerable", () -> new PlannedStatusEffect(MobEffectCategory.HARMFUL, 0xFF6A34));
    public static final RegistryObject<MobEffect> DEPARTMENT_CONCEALMENT =
            EFFECTS.register("department_concealment", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x9CEBFF));
    public static final RegistryObject<MobEffect> UNDEAD_TRUE_INVISIBILITY =
            EFFECTS.register("undead_true_invisibility", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x65758A));
    public static final RegistryObject<MobEffect> UNDEAD_HUNTER_SCATTER =
            EFFECTS.register("undead_hunter_scatter", () -> new PlannedStatusEffect(MobEffectCategory.BENEFICIAL, 0x4A8DFF));

}
