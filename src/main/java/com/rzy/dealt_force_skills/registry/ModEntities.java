package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import com.rzy.dealt_force_skills.entity.BladeWireProjectileEntity;
import com.rzy.dealt_force_skills.entity.CatDadRoadTruckEntity;
import com.rzy.dealt_force_skills.entity.DepartmentExplosiveTrapEntity;
import com.rzy.dealt_force_skills.entity.DepartmentOverheatLaserEntity;
import com.rzy.dealt_force_skills.entity.DWolfHandCannonGrenadeEntity;
import com.rzy.dealt_force_skills.entity.DWolfSmokeCloudEntity;
import com.rzy.dealt_force_skills.entity.DWolfSmokeGrenadeEntity;
import com.rzy.dealt_force_skills.entity.GizmoSmokeCloudEntity;
import com.rzy.dealt_force_skills.entity.GizmoSmokeTrapEntity;
import com.rzy.dealt_force_skills.entity.GizmoSpiderNestTrapEntity;
import com.rzy.dealt_force_skills.entity.GizmoSpiderlingEntity;
import com.rzy.dealt_force_skills.entity.GizmoTBoyEntity;
import com.rzy.dealt_force_skills.entity.GrappleHookEntity;
import com.rzy.dealt_force_skills.entity.HackclawFlashDroneEntity;
import com.rzy.dealt_force_skills.entity.HackclawInterferenceFieldEntity;
import com.rzy.dealt_force_skills.entity.HackclawKnifeEntity;
import com.rzy.dealt_force_skills.entity.LunaCompositeGrenadeEntity;
import com.rzy.dealt_force_skills.entity.LunaReconArrowEntity;
import com.rzy.dealt_force_skills.entity.LunaShockArrowEntity;
import com.rzy.dealt_force_skills.entity.MorseFlashGrenadeEntity;
import com.rzy.dealt_force_skills.entity.MorseShockOrbEntity;
import com.rzy.dealt_force_skills.entity.MorseSonarDetectorEntity;
import com.rzy.dealt_force_skills.entity.NoxDecoyEntity;
import com.rzy.dealt_force_skills.entity.NoxFlashGrenadeEntity;
import com.rzy.dealt_force_skills.entity.NoxRotorDroneEntity;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.RaptorPulseGrenadeEntity;
import com.rzy.dealt_force_skills.entity.ShepherdDroneEntity;
import com.rzy.dealt_force_skills.entity.ShepherdFragGrenadeEntity;
import com.rzy.dealt_force_skills.entity.ShepherdSonicTrapEntity;
import com.rzy.dealt_force_skills.entity.StingerSmokeCloudEntity;
import com.rzy.dealt_force_skills.entity.StingerSmokeDroneEntity;
import com.rzy.dealt_force_skills.entity.StingerSmokeGrenadeEntity;
import com.rzy.dealt_force_skills.entity.StingerStimProjectileEntity;
import com.rzy.dealt_force_skills.entity.TempestWallDrillStingerEntity;
import com.rzy.dealt_force_skills.entity.TempestRecallAnchorEntity;
import com.rzy.dealt_force_skills.entity.ToxikFireflyEntity;
import com.rzy.dealt_force_skills.entity.ToxikTearGasCloudEntity;
import com.rzy.dealt_force_skills.entity.ToxikTearGasGrenadeEntity;
import com.rzy.dealt_force_skills.entity.UluruBombletEntity;
import com.rzy.dealt_force_skills.entity.UluruFireFieldEntity;
import com.rzy.dealt_force_skills.entity.UluruIncendiaryGrenadeEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.entity.UluruQuickCoverPackageEntity;
import com.rzy.dealt_force_skills.entity.VlinderActiveDefenseDroneEntity;
import com.rzy.dealt_force_skills.entity.VlinderMedicalDroneEntity;
import com.rzy.dealt_force_skills.entity.VlinderRemoteSmokeRoundEntity;
import com.rzy.dealt_force_skills.entity.VyronMagneticBombEntity;
import com.rzy.dealt_force_skills.entity.VyronTigerCannonEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, DealtForceSkillsMod.MODID);

    public static final RegistryObject<EntityType<GrappleHookEntity>> GRAPPLE_HOOK =
            ENTITIES.register("grapple_hook",
                    () -> EntityType.Builder.<GrappleHookEntity>of(GrappleHookEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("grapple_hook"));

    public static final RegistryObject<EntityType<BladeWireProjectileEntity>> BLADE_WIRE_PROJECTILE =
            ENTITIES.register("blade_wire_projectile",
                    () -> EntityType.Builder.<BladeWireProjectileEntity>of(BladeWireProjectileEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("blade_wire_projectile"));

    public static final RegistryObject<EntityType<UluruIncendiaryGrenadeEntity>> ULURU_INCENDIARY_GRENADE =
            ENTITIES.register("uluru_incendiary_grenade",
                    () -> EntityType.Builder.<UluruIncendiaryGrenadeEntity>of(UluruIncendiaryGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("uluru_incendiary_grenade"));

    public static final RegistryObject<EntityType<UluruFireFieldEntity>> ULURU_FIRE_FIELD =
            ENTITIES.register("uluru_fire_field",
                    () -> EntityType.Builder.<UluruFireFieldEntity>of(UluruFireFieldEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(64)
                            .updateInterval(5)
                            .build("uluru_fire_field"));

    public static final RegistryObject<EntityType<UluruQuickCoverPackageEntity>> ULURU_QUICK_COVER_PACKAGE =
            ENTITIES.register("uluru_quick_cover_package",
                    () -> EntityType.Builder.<UluruQuickCoverPackageEntity>of(UluruQuickCoverPackageEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.35f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("uluru_quick_cover_package"));

    public static final RegistryObject<EntityType<UluruLoiteringMissileEntity>> ULURU_LOITERING_MISSILE =
            ENTITIES.register("uluru_loitering_missile",
                    () -> EntityType.Builder.<UluruLoiteringMissileEntity>of(UluruLoiteringMissileEntity::new, MobCategory.MISC)
                            .sized(0.45f, 0.45f)
                            .clientTrackingRange(256)
                            .updateInterval(1)
                            .build("uluru_loitering_missile"));

    public static final RegistryObject<EntityType<UluruBombletEntity>> ULURU_BOMBLET =
            ENTITIES.register("uluru_bomblet",
                    () -> EntityType.Builder.<UluruBombletEntity>of(UluruBombletEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("uluru_bomblet"));

    public static final RegistryObject<EntityType<DWolfHandCannonGrenadeEntity>> D_WOLF_HAND_CANNON_GRENADE =
            ENTITIES.register("d_wolf_hand_cannon_grenade",
                    () -> EntityType.Builder.<DWolfHandCannonGrenadeEntity>of(DWolfHandCannonGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("d_wolf_hand_cannon_grenade"));

    public static final RegistryObject<EntityType<DWolfSmokeGrenadeEntity>> D_WOLF_SMOKE_GRENADE =
            ENTITIES.register("d_wolf_smoke_grenade",
                    () -> EntityType.Builder.<DWolfSmokeGrenadeEntity>of(DWolfSmokeGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("d_wolf_smoke_grenade"));

    public static final RegistryObject<EntityType<DWolfSmokeCloudEntity>> D_WOLF_SMOKE_CLOUD =
            ENTITIES.register("d_wolf_smoke_cloud",
                    () -> EntityType.Builder.<DWolfSmokeCloudEntity>of(DWolfSmokeCloudEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(96)
                            .updateInterval(5)
                            .build("d_wolf_smoke_cloud"));

    public static final RegistryObject<EntityType<GizmoSmokeTrapEntity>> GIZMO_SMOKE_TRAP =
            ENTITIES.register("gizmo_smoke_trap",
                    () -> EntityType.Builder.<GizmoSmokeTrapEntity>of(GizmoSmokeTrapEntity::new, MobCategory.MISC)
                            .sized(0.38f, 0.20f)
                            .clientTrackingRange(96)
                            .updateInterval(2)
                            .build("gizmo_smoke_trap"));

    public static final RegistryObject<EntityType<GizmoSmokeCloudEntity>> GIZMO_SMOKE_CLOUD =
            ENTITIES.register("gizmo_smoke_cloud",
                    () -> EntityType.Builder.<GizmoSmokeCloudEntity>of(GizmoSmokeCloudEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(96)
                            .updateInterval(5)
                            .build("gizmo_smoke_cloud"));

    public static final RegistryObject<EntityType<GizmoSpiderNestTrapEntity>> GIZMO_SPIDER_NEST_TRAP =
            ENTITIES.register("gizmo_spider_nest_trap",
                    () -> EntityType.Builder.<GizmoSpiderNestTrapEntity>of(GizmoSpiderNestTrapEntity::new, MobCategory.MISC)
                            .sized(0.42f, 0.24f)
                            .clientTrackingRange(96)
                            .updateInterval(2)
                            .build("gizmo_spider_nest_trap"));

    public static final RegistryObject<EntityType<GizmoSpiderlingEntity>> GIZMO_SPIDERLING =
            ENTITIES.register("gizmo_spiderling",
                    () -> EntityType.Builder.<GizmoSpiderlingEntity>of(GizmoSpiderlingEntity::new, MobCategory.MISC)
                            .sized(0.4f, 0.3f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("gizmo_spiderling"));

    public static final RegistryObject<EntityType<GizmoTBoyEntity>> GIZMO_T_BOY =
            ENTITIES.register("gizmo_t_boy",
                    () -> EntityType.Builder.<GizmoTBoyEntity>of(GizmoTBoyEntity::new, MobCategory.MISC)
                            .sized(0.35f, 0.3f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("gizmo_t_boy"));

    public static final RegistryObject<EntityType<ShepherdSonicTrapEntity>> SHEPHERD_SONIC_TRAP =
            ENTITIES.register("shepherd_sonic_trap",
                    () -> EntityType.Builder.<ShepherdSonicTrapEntity>of(ShepherdSonicTrapEntity::new, MobCategory.MISC)
                            .sized(0.38f, 0.22f)
                            .clientTrackingRange(96)
                            .updateInterval(2)
                            .build("shepherd_sonic_trap"));

    public static final RegistryObject<EntityType<ShepherdFragGrenadeEntity>> SHEPHERD_FRAG_GRENADE =
            ENTITIES.register("shepherd_frag_grenade",
                    () -> EntityType.Builder.<ShepherdFragGrenadeEntity>of(ShepherdFragGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("shepherd_frag_grenade"));

    public static final RegistryObject<EntityType<ShepherdDroneEntity>> SHEPHERD_DRONE =
            ENTITIES.register("shepherd_drone",
                    () -> EntityType.Builder.<ShepherdDroneEntity>of(ShepherdDroneEntity::new, MobCategory.MISC)
                            .sized(0.52f, 0.32f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("shepherd_drone"));

    public static final RegistryObject<EntityType<LunaShockArrowEntity>> LUNA_SHOCK_ARROW =
            ENTITIES.register("luna_shock_arrow",
                    () -> EntityType.Builder.<LunaShockArrowEntity>of(LunaShockArrowEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("luna_shock_arrow"));

    public static final RegistryObject<EntityType<LunaCompositeGrenadeEntity>> LUNA_COMPOSITE_GRENADE =
            ENTITIES.register("luna_composite_grenade",
                    () -> EntityType.Builder.<LunaCompositeGrenadeEntity>of(LunaCompositeGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("luna_composite_grenade"));

    public static final RegistryObject<EntityType<LunaReconArrowEntity>> LUNA_RECON_ARROW =
            ENTITIES.register("luna_recon_arrow",
                    () -> EntityType.Builder.<LunaReconArrowEntity>of(LunaReconArrowEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("luna_recon_arrow"));

    public static final RegistryObject<EntityType<HackclawKnifeEntity>> HACKCLAW_KNIFE =
            ENTITIES.register("hackclaw_knife",
                    () -> EntityType.Builder.<HackclawKnifeEntity>of(HackclawKnifeEntity::new, MobCategory.MISC)
                            .sized(0.24f, 0.24f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("hackclaw_knife"));

    public static final RegistryObject<EntityType<HackclawInterferenceFieldEntity>> HACKCLAW_INTERFERENCE_FIELD =
            ENTITIES.register("hackclaw_interference_field",
                    () -> EntityType.Builder.<HackclawInterferenceFieldEntity>of(
                                    HackclawInterferenceFieldEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(96)
                            .updateInterval(5)
                            .build("hackclaw_interference_field"));

    public static final RegistryObject<EntityType<HackclawFlashDroneEntity>> HACKCLAW_FLASH_DRONE =
            ENTITIES.register("hackclaw_flash_drone",
                    () -> EntityType.Builder.<HackclawFlashDroneEntity>of(
                                    HackclawFlashDroneEntity::new, MobCategory.MISC)
                            .sized(0.42f, 0.28f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("hackclaw_flash_drone"));

    public static final RegistryObject<EntityType<VyronMagneticBombEntity>> VYRON_MAGNETIC_BOMB =
            ENTITIES.register("vyron_magnetic_bomb",
                    () -> EntityType.Builder.<VyronMagneticBombEntity>of(VyronMagneticBombEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("vyron_magnetic_bomb"));

    public static final RegistryObject<EntityType<VyronTigerCannonEntity>> VYRON_TIGER_CANNON =
            ENTITIES.register("vyron_tiger_cannon",
                    () -> EntityType.Builder.<VyronTigerCannonEntity>of(VyronTigerCannonEntity::new, MobCategory.MISC)
                            .sized(0.36f, 0.36f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("vyron_tiger_cannon"));

    public static final RegistryObject<EntityType<NoxRotorDroneEntity>> NOX_ROTOR_DRONE =
            ENTITIES.register("nox_rotor_drone",
                    () -> EntityType.Builder.<NoxRotorDroneEntity>of(NoxRotorDroneEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("nox_rotor_drone"));

    public static final RegistryObject<EntityType<NoxFlashGrenadeEntity>> NOX_FLASH_GRENADE =
            ENTITIES.register("nox_flash_grenade",
                    () -> EntityType.Builder.<NoxFlashGrenadeEntity>of(NoxFlashGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("nox_flash_grenade"));

    public static final RegistryObject<EntityType<NoxDecoyEntity>> NOX_DECOY =
            ENTITIES.register("nox_decoy",
                    () -> EntityType.Builder.<NoxDecoyEntity>of(NoxDecoyEntity::new, MobCategory.MISC)
                            .sized(0.6f, 1.8f)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("nox_decoy"));

    public static final RegistryObject<EntityType<MorseShockOrbEntity>> MORSE_SHOCK_ORB =
            ENTITIES.register("morse_shock_orb",
                    () -> EntityType.Builder.<MorseShockOrbEntity>of(MorseShockOrbEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("morse_shock_orb"));

    public static final RegistryObject<EntityType<MorseFlashGrenadeEntity>> MORSE_FLASH_GRENADE =
            ENTITIES.register("morse_flash_grenade",
                    () -> EntityType.Builder.<MorseFlashGrenadeEntity>of(MorseFlashGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("morse_flash_grenade"));

    public static final RegistryObject<EntityType<MorseSonarDetectorEntity>> MORSE_SONAR_DETECTOR =
            ENTITIES.register("morse_sonar_detector",
                    () -> EntityType.Builder.<MorseSonarDetectorEntity>of(MorseSonarDetectorEntity::new, MobCategory.MISC)
                            .sized(0.42f, 0.24f)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("morse_sonar_detector"));

    public static final RegistryObject<EntityType<StingerSmokeGrenadeEntity>> STINGER_SMOKE_GRENADE =
            ENTITIES.register("stinger_smoke_grenade",
                    () -> EntityType.Builder.<StingerSmokeGrenadeEntity>of(StingerSmokeGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("stinger_smoke_grenade"));

    public static final RegistryObject<EntityType<StingerSmokeCloudEntity>> STINGER_SMOKE_CLOUD =
            ENTITIES.register("stinger_smoke_cloud",
                    () -> EntityType.Builder.<StingerSmokeCloudEntity>of(StingerSmokeCloudEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(96)
                            .updateInterval(5)
                            .build("stinger_smoke_cloud"));

    public static final RegistryObject<EntityType<StingerSmokeDroneEntity>> STINGER_SMOKE_DRONE =
            ENTITIES.register("stinger_smoke_drone",
                    () -> EntityType.Builder.<StingerSmokeDroneEntity>of(StingerSmokeDroneEntity::new, MobCategory.MISC)
                            .sized(0.52f, 0.32f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("stinger_smoke_drone"));

    public static final RegistryObject<EntityType<StingerStimProjectileEntity>> STINGER_STIM_PROJECTILE =
            ENTITIES.register("stinger_stim_projectile",
                    () -> EntityType.Builder.<StingerStimProjectileEntity>of(StingerStimProjectileEntity::new, MobCategory.MISC)
                            .sized(0.24f, 0.24f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("stinger_stim_projectile"));

    public static final RegistryObject<EntityType<ToxikTearGasGrenadeEntity>> TOXIK_TEAR_GAS_GRENADE =
            ENTITIES.register("toxik_tear_gas_grenade",
                    () -> EntityType.Builder.<ToxikTearGasGrenadeEntity>of(ToxikTearGasGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("toxik_tear_gas_grenade"));

    public static final RegistryObject<EntityType<ToxikTearGasCloudEntity>> TOXIK_TEAR_GAS_CLOUD =
            ENTITIES.register("toxik_tear_gas_cloud",
                    () -> EntityType.Builder.<ToxikTearGasCloudEntity>of(ToxikTearGasCloudEntity::new, MobCategory.MISC)
                            .sized(0.1f, 0.1f)
                            .clientTrackingRange(96)
                            .updateInterval(5)
                            .build("toxik_tear_gas_cloud"));

    public static final RegistryObject<EntityType<ToxikFireflyEntity>> TOXIK_FIREFLY =
            ENTITIES.register("toxik_firefly",
                    () -> EntityType.Builder.<ToxikFireflyEntity>of(ToxikFireflyEntity::new, MobCategory.MISC)
                            .sized(0.18f, 0.18f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("toxik_firefly"));

    public static final RegistryObject<EntityType<RaptorPulseGrenadeEntity>> RAPTOR_PULSE_GRENADE =
            ENTITIES.register("raptor_pulse_grenade",
                    () -> EntityType.Builder.<RaptorPulseGrenadeEntity>of(RaptorPulseGrenadeEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(96)
                            .updateInterval(1)
                            .build("raptor_pulse_grenade"));

    public static final RegistryObject<EntityType<RaptorFalconDroneEntity>> RAPTOR_FALCON_DRONE =
            ENTITIES.register("raptor_falcon_drone",
                    () -> EntityType.Builder.<RaptorFalconDroneEntity>of(RaptorFalconDroneEntity::new, MobCategory.MISC)
                            .sized(0.44f, 0.28f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("raptor_falcon_drone"));

    public static final RegistryObject<EntityType<VlinderMedicalDroneEntity>> VLINDER_MEDICAL_DRONE =
            ENTITIES.register("vlinder_medical_drone",
                    () -> EntityType.Builder.<VlinderMedicalDroneEntity>of(VlinderMedicalDroneEntity::new, MobCategory.MISC)
                            .sized(0.42f, 0.28f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("vlinder_medical_drone"));

    public static final RegistryObject<EntityType<VlinderRemoteSmokeRoundEntity>> VLINDER_REMOTE_SMOKE_ROUND =
            ENTITIES.register("vlinder_remote_smoke_round",
                    () -> EntityType.Builder.<VlinderRemoteSmokeRoundEntity>of(VlinderRemoteSmokeRoundEntity::new, MobCategory.MISC)
                            .sized(0.32f, 0.32f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("vlinder_remote_smoke_round"));

    public static final RegistryObject<EntityType<VlinderActiveDefenseDroneEntity>> VLINDER_ACTIVE_DEFENSE_DRONE =
            ENTITIES.register("vlinder_active_defense_drone",
                    () -> EntityType.Builder.<VlinderActiveDefenseDroneEntity>of(VlinderActiveDefenseDroneEntity::new, MobCategory.MISC)
                            .sized(0.52f, 0.34f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("vlinder_active_defense_drone"));

    public static final RegistryObject<EntityType<TempestWallDrillStingerEntity>> TEMPEST_WALL_DRILL_STINGER =
            ENTITIES.register("tempest_wall_drill_stinger",
                    () -> EntityType.Builder.<TempestWallDrillStingerEntity>of(TempestWallDrillStingerEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("tempest_wall_drill_stinger"));
    public static final RegistryObject<EntityType<TempestRecallAnchorEntity>> TEMPEST_RECALL_ANCHOR =
            ENTITIES.register("tempest_recall_anchor",
                    () -> EntityType.Builder.<TempestRecallAnchorEntity>of(
                                    TempestRecallAnchorEntity::new, MobCategory.MISC)
                            .sized(0.7f, 0.25f)
                            .clientTrackingRange(320)
                            .updateInterval(1)
                            .build("tempest_recall_anchor"));
    public static final RegistryObject<EntityType<CatDadRoadTruckEntity>> CATDAD_ROAD_TRUCK =
            ENTITIES.register("catdad_road_truck",
                    () -> EntityType.Builder.<CatDadRoadTruckEntity>of(CatDadRoadTruckEntity::new, MobCategory.MISC)
                            .sized(8.0f, 3.0f)
                            .clientTrackingRange(192)
                            .updateInterval(1)
                            .build("catdad_road_truck"));

    public static final RegistryObject<EntityType<DepartmentOverheatLaserEntity>> DEPARTMENT_OVERHEAT_LASER =
            ENTITIES.register("department_overheat_laser",
                    () -> EntityType.Builder.<DepartmentOverheatLaserEntity>of(DepartmentOverheatLaserEntity::new, MobCategory.MISC)
                            .sized(0.18f, 0.18f)
                            .clientTrackingRange(128)
                            .updateInterval(1)
                            .build("department_overheat_laser"));

    public static final RegistryObject<EntityType<DepartmentExplosiveTrapEntity>> DEPARTMENT_EXPLOSIVE_TRAP =
            ENTITIES.register("department_explosive_trap",
                    () -> EntityType.Builder.<DepartmentExplosiveTrapEntity>of(DepartmentExplosiveTrapEntity::new, MobCategory.MISC)
                            .sized(0.42f, 0.36f)
                            .clientTrackingRange(128)
                            .updateInterval(2)
                            .build("department_explosive_trap"));
}
