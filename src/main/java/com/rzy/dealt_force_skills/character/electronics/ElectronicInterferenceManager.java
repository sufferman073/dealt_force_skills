package com.rzy.dealt_force_skills.character.electronics;

import com.rzy.dealt_force_skills.character.CharacterDefinition;
import com.rzy.dealt_force_skills.character.ModCharacters;
import com.rzy.dealt_force_skills.character.SkillSlot;
import com.rzy.dealt_force_skills.entity.GizmoSmokeTrapEntity;
import com.rzy.dealt_force_skills.entity.GizmoSpiderNestTrapEntity;
import com.rzy.dealt_force_skills.entity.GizmoSpiderlingEntity;
import com.rzy.dealt_force_skills.entity.GizmoTBoyEntity;
import com.rzy.dealt_force_skills.entity.HackclawFlashDroneEntity;
import com.rzy.dealt_force_skills.entity.HackclawInterferenceFieldEntity;
import com.rzy.dealt_force_skills.entity.MorseSonarDetectorEntity;
import com.rzy.dealt_force_skills.entity.NoxRotorDroneEntity;
import com.rzy.dealt_force_skills.entity.RaptorFalconDroneEntity;
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
        if (character == null || slot == SkillSlot.PASSIVE || ModCharacters.SINEVA_ID.equals(character.id())) {
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
        return entity instanceof GizmoSmokeTrapEntity
                || entity instanceof GizmoSpiderNestTrapEntity
                || entity instanceof GizmoSpiderlingEntity
                || entity instanceof GizmoTBoyEntity
                || entity instanceof HackclawFlashDroneEntity
                || entity instanceof MorseSonarDetectorEntity
                || entity instanceof ShepherdSonicTrapEntity
                || entity instanceof ShepherdDroneEntity
                || entity instanceof NoxRotorDroneEntity
                || entity instanceof RaptorFalconDroneEntity
                || entity instanceof StingerSmokeDroneEntity
                || entity instanceof VlinderMedicalDroneEntity
                || entity instanceof VlinderRemoteSmokeRoundEntity
                || entity instanceof VlinderActiveDefenseDroneEntity
                || entity instanceof TempestWallDrillStingerEntity
                || entity instanceof UluruLoiteringMissileEntity;
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
