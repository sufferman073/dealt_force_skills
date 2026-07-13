package com.rzy.dealt_force_skills.compat;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.ModList;

import java.lang.reflect.Method;
import java.util.List;

public final class SuperbWarfareCompat {
    private static final String MOD_ID = "superbwarfare";
    private static final String VEHICLE_ENTITY_CLASS = "com.atsuishio.superbwarfare.entity.vehicle.base.VehicleEntity";
    private static final float MAGNETIC_BOMB_MAX_HEALTH_FRACTION = 0.25F;

    private static volatile boolean vehicleClassResolved;
    private static volatile Class<?> vehicleClass;
    private static volatile Method getMaxHealthMethod;

    private SuperbWarfareCompat() {
    }

    public static void damageVehicles(ServerLevel level, Vec3 center, double radius, DamageSource source,
                                      Entity excludedEntity, float magneticBombEquivalents,
                                      boolean requireLineOfSight) {
        if (level == null || center == null || source == null || radius <= 0.0D
                || magneticBombEquivalents <= 0.0F || !isLoaded()) {
            return;
        }

        AABB searchBox = new AABB(center, center).inflate(radius);
        for (Entity entity : level.getEntities((Entity) null, searchBox,
                entity -> entity != excludedEntity && isVehicle(entity) && !entity.isRemoved())) {
            AABB bounds = entity.getBoundingBox();
            Vec3 hitPoint = closestPoint(center, bounds);
            double distance = hitPoint.distanceTo(center);
            if (distance > radius) {
                continue;
            }
            if (requireLineOfSight && !hasLineOfSight(level, center, hitPoint, entity)) {
                continue;
            }

            float maxHealth = maxHealth(entity);
            if (maxHealth <= 0.0F) {
                continue;
            }
            float falloff = Math.max(0.0F, 1.0F - (float) (distance / radius));
            float damage = maxHealth * MAGNETIC_BOMB_MAX_HEALTH_FRACTION * magneticBombEquivalents * falloff;
            if (damage <= 0.0F) {
                continue;
            }
            entity.hurt(source, damage);
            entity.hurtMarked = true;
        }
    }

    public static void removeVehicles(List<Entity> entities) {
        if (entities == null || entities.isEmpty() || !isLoaded()) {
            return;
        }
        entities.removeIf(SuperbWarfareCompat::isVehicle);
    }

    public static boolean isVehicle(Entity entity) {
        Class<?> type = vehicleClass();
        return entity != null && type != null && type.isInstance(entity);
    }

    private static boolean isLoaded() {
        return ModList.get().isLoaded(MOD_ID);
    }

    private static Class<?> vehicleClass() {
        if (!isLoaded()) {
            return null;
        }
        if (!vehicleClassResolved) {
            synchronized (SuperbWarfareCompat.class) {
                if (!vehicleClassResolved) {
                    try {
                        vehicleClass = Class.forName(VEHICLE_ENTITY_CLASS);
                        getMaxHealthMethod = vehicleClass.getMethod("getMaxHealth");
                    } catch (ReflectiveOperationException | LinkageError ignored) {
                        vehicleClass = null;
                        getMaxHealthMethod = null;
                    }
                    vehicleClassResolved = true;
                }
            }
        }
        return vehicleClass;
    }

    private static float maxHealth(Entity entity) {
        Method method = getMaxHealthMethod;
        if (method == null || !isVehicle(entity)) {
            return 0.0F;
        }
        try {
            Object value = method.invoke(entity);
            return value instanceof Number number ? number.floatValue() : 0.0F;
        } catch (ReflectiveOperationException | LinkageError ignored) {
            return 0.0F;
        }
    }

    private static Vec3 closestPoint(Vec3 point, AABB bounds) {
        double x = Math.max(bounds.minX, Math.min(point.x, bounds.maxX));
        double y = Math.max(bounds.minY, Math.min(point.y, bounds.maxY));
        double z = Math.max(bounds.minZ, Math.min(point.z, bounds.maxZ));
        return new Vec3(x, y, z);
    }

    private static boolean hasLineOfSight(ServerLevel level, Vec3 center, Vec3 target, Entity context) {
        if (center.distanceToSqr(target) < 1.0E-4D) {
            return true;
        }
        HitResult result = level.clip(new ClipContext(center, target, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, context));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(target) < 0.25D;
    }
}
