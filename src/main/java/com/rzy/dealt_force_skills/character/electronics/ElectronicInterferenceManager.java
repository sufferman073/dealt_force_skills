package com.rzy.dealt_force_skills.character.electronics;

import com.rzy.dealt_force_skills.character.CharacterDefinition;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.rzy.dealt_force_skills.entity.GizmoSmokeTrapEntity;
import com.rzy.dealt_force_skills.entity.GizmoSpiderNestTrapEntity;
import com.rzy.dealt_force_skills.entity.GizmoSpiderlingEntity;
import com.rzy.dealt_force_skills.entity.GizmoTBoyEntity;
import com.rzy.dealt_force_skills.entity.HackclawFlashDroneEntity;
import com.rzy.dealt_force_skills.entity.HackclawInterferenceFieldEntity;
import com.rzy.dealt_force_skills.entity.MorseSonarDetectorEntity;
import com.rzy.dealt_force_skills.entity.NoxRotorDroneEntity;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
import com.rzy.dealt_force_skills.entity.SaeedGuardEntity;
import com.rzy.dealt_force_skills.entity.ShepherdDroneEntity;
import com.rzy.dealt_force_skills.entity.ShepherdSonicTrapEntity;
import com.rzy.dealt_force_skills.entity.StingerSmokeDroneEntity;
import com.rzy.dealt_force_skills.entity.TempestWallDrillStingerEntity;
import com.rzy.dealt_force_skills.entity.UluruLoiteringMissileEntity;
import com.rzy.dealt_force_skills.entity.VlinderActiveDefenseDroneEntity;
import com.rzy.dealt_force_skills.entity.VlinderMedicalDroneEntity;
import com.rzy.dealt_force_skills.entity.VlinderRemoteSmokeRoundEntity;
import com.rzy.dealt_force_skills.registry.ModEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class ElectronicInterferenceManager {
    private ElectronicInterferenceManager() {
    }

    public static boolean tryBlockSkillUse(ServerPlayer player, CharacterDefinition character, SkillSlot slot) {
        if (isElectronicSkill(character, slot)
                && (player.hasEffect(ModEffects.RAPTOR_ELECTROMAGNETIC_INTERFERENCE.get())
                || player.hasEffect(ModEffects.VLINDER_MEDICAL_WASTE_INTERFERENCE.get()))) {
            player.displayClientMessage(Component.translatable(
                    "message.dealt_force_skills.electronic_interference_blocked"), true);
            return true;
        }

        HackclawInterferenceFieldEntity field = blockingField(player, character, slot);
        if (field == null) {
            return false;
        }

        player.displayClientMessage(Component.translatable(
                "message.dealt_force_skills.electronic_interference_blocked"), true);
        field.markSuccessfulInterference();
        return true;
    }

    public static HackclawInterferenceFieldEntity blockingField(
            ServerPlayer player,
            CharacterDefinition character,
            SkillSlot slot
    ) {
        if (!isElectronicSkill(character, slot) || !(player.level() instanceof ServerLevel level)) {
            return null;
        }

        AABB search = player.getBoundingBox().inflate(HackclawInterferenceFieldEntity.RADIUS);
        for (HackclawInterferenceFieldEntity field : level.getEntitiesOfClass(
                HackclawInterferenceFieldEntity.class, search, HackclawInterferenceFieldEntity::isActive)) {
            if (field.position().distanceToSqr(player.position()) <= field.radius() * field.radius()) {
                return field;
            }
        }
        return null;
    }

    public static int disruptElectronicDevices(HackclawInterferenceFieldEntity field) {
        if (!(field.level() instanceof ServerLevel level) || !field.isActive()) {
            return 0;
        }

        AABB box = new AABB(field.position(), field.position()).inflate(field.radius());
        int destroyed = 0;
        for (Entity entity : level.getEntities(field, box, ElectronicInterferenceManager::isDestroyableElectronicDevice)) {
            if (!entity.isRemoved()) {
                discardElectronicDevice(entity);
                destroyed++;
            }
        }
        if (destroyed > 0) {
            field.markSuccessfulInterference();
        }
        return destroyed;
    }

    public static int disruptElectronicDevices(ServerLevel level, Vec3 center, double radius) {
        AABB box = new AABB(center, center).inflate(radius);
        int destroyed = 0;
        for (Entity entity : level.getEntities((Entity) null, box, ElectronicInterferenceManager::isDestroyableElectronicDevice)) {
            if (!entity.isRemoved() && entity.position().distanceTo(center) <= radius) {
                discardElectronicDevice(entity);
                destroyed++;
            }
        }
        return destroyed;
    }

    public static boolean isElectronicSkill(CharacterDefinition character, SkillSlot slot) {
        if (character == null || slot == null) {
            return false;
        }

        String id = character.id();
        int separator = id.lastIndexOf('/');
        String characterKey = separator >= 0 ? id.substring(separator + 1) : id.substring(id.indexOf(':') + 1);
        return DealtForceConfig.booleanValue(
                "characters." + characterKey + ".skills." + slot.name().toLowerCase() + ".electronic",
                defaultElectronicSkill(character, slot));
    }

    public static void populateConfigDefaults() {
        for (CharacterDefinition character : ModCharacters.all()) {
            for (SkillSlot slot : SkillSlot.values()) {
                isElectronicSkill(character, slot);
            }
        }
        String[] devices = {
                "gizmo_smoke_trap", "gizmo_spider_nest", "gizmo_spiderling", "gizmo_t_boy",
                "hackclaw_flash_drone", "morse_sonar_detector", "shepherd_sonic_trap", "shepherd_drone",
                "nox_rotor_drone", "raptor_falcon_drone", "stinger_smoke_drone", "vlinder_medical_drone",
                "vlinder_remote_smoke_round", "vlinder_active_defense_drone", "tempest_wall_drill_stinger",
                "uluru_loitering_missile"
        };
        for (String device : devices) {
            DealtForceConfig.booleanValue("summons." + device + ".electronic", true);
        }
        DealtForceConfig.booleanValue("summons.saeed_guard.electronic", false);
    }

    private static boolean defaultElectronicSkill(CharacterDefinition character, SkillSlot slot) {
        if (slot == SkillSlot.PASSIVE || ModCharacters.SINEVA_ID.equals(character.id())) {
            return false;
        }

        String id = character.id();
        if (ModCharacters.HACKCLAW_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_2 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.ULURU_ID.equals(id)) {
            return slot == SkillSlot.CORE;
        }
        if (ModCharacters.D_WOLF_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.GIZMO_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.ACTIVE_2 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.SHEPHERD_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.LUNA_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.VYRON_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.NOX_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.TEMPEST_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_2 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.STINGER_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_2 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.TOXIK_ID.equals(id)) {
            return false;
        }
        if (ModCharacters.VLINDER_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.ACTIVE_2 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.MORSE_ID.equals(id)) {
            return slot == SkillSlot.CORE;
        }
        if (ModCharacters.RAPTOR_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_1 || slot == SkillSlot.CORE;
        }
        if (ModCharacters.MANBA_ID.equals(id)) {
            return slot == SkillSlot.ACTIVE_2;
        }
        return slot != SkillSlot.PASSIVE;
    }

    private static boolean isDestroyableElectronicDevice(Entity entity) {
        return configuredDevice(entity, GizmoSmokeTrapEntity.class, "gizmo_smoke_trap", true)
                || configuredDevice(entity, GizmoSpiderNestTrapEntity.class, "gizmo_spider_nest", true)
                || configuredDevice(entity, GizmoSpiderlingEntity.class, "gizmo_spiderling", true)
                || configuredDevice(entity, GizmoTBoyEntity.class, "gizmo_t_boy", true)
                || configuredDevice(entity, HackclawFlashDroneEntity.class, "hackclaw_flash_drone", true)
                || configuredDevice(entity, MorseSonarDetectorEntity.class, "morse_sonar_detector", true)
                || configuredDevice(entity, ShepherdSonicTrapEntity.class, "shepherd_sonic_trap", true)
                || configuredDevice(entity, ShepherdDroneEntity.class, "shepherd_drone", true)
                || configuredDevice(entity, NoxRotorDroneEntity.class, "nox_rotor_drone", true)
                || configuredDevice(entity, RaptorFalconDroneEntity.class, "raptor_falcon_drone", true)
                || configuredDevice(entity, StingerSmokeDroneEntity.class, "stinger_smoke_drone", true)
                || configuredDevice(entity, VlinderMedicalDroneEntity.class, "vlinder_medical_drone", true)
                || configuredDevice(entity, VlinderRemoteSmokeRoundEntity.class, "vlinder_remote_smoke_round", true)
                || configuredDevice(entity, VlinderActiveDefenseDroneEntity.class, "vlinder_active_defense_drone", true)
                || configuredDevice(entity, TempestWallDrillStingerEntity.class, "tempest_wall_drill_stinger", true)
                || configuredDevice(entity, UluruLoiteringMissileEntity.class, "uluru_loitering_missile", true)
                || configuredDevice(entity, SaeedGuardEntity.class, "saeed_guard", false);
    }

    private static boolean configuredDevice(Entity entity, Class<? extends Entity> type, String key, boolean defaultValue) {
        return type.isInstance(entity)
                && DealtForceConfig.booleanValue("summons." + key + ".electronic", defaultValue);
    }

    private static void discardElectronicDevice(Entity entity) {
        if (entity instanceof ShepherdSonicTrapEntity sonicTrap) {
            sonicTrap.destroyByInterference();
            return;
        }
        if (entity instanceof RaptorFalconDroneEntity falcon) {
            falcon.destroyByInterference();
            return;
        }
        if (entity instanceof MorseSonarDetectorEntity sonar) {
            sonar.destroyByInterference();
            return;
        }
        if (entity instanceof VlinderMedicalDroneEntity medicalDrone) {
            medicalDrone.destroyByInterference();
            return;
        }
        if (entity instanceof VlinderActiveDefenseDroneEntity activeDefenseDrone) {
            activeDefenseDrone.destroyByInterference();
            return;
        }
        if (entity instanceof TempestWallDrillStingerEntity wallDrill) {
            wallDrill.destroyByInterference();
            return;
        }
        entity.discard();
    }
}
