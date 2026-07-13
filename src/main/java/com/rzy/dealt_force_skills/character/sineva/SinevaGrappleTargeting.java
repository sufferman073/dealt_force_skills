package com.rzy.dealt_force_skills.character.sineva;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class SinevaGrappleTargeting {
    private static volatile double MAX_DISTANCE_SQR = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("MAX_DISTANCE_SQR", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.grapplehookentity.max_distance_sqr", 961.0));
    private static final double LOCK_CONE_TAN = Math.tan(Math.toRadians(4.5D));
    private static final double MIN_LOCK_RADIUS = 0.75D;
    private static final double TARGET_RADIUS_PADDING = 0.45D;
    private static final double SEARCH_PADDING = 4.0D;

    private SinevaGrappleTargeting() {
    }

    public static LivingEntity findTarget(Level level, LivingEntity owner) {
        if (level == null || owner == null || !owner.isAlive()) {
            return null;
        }
        Vec3 look = owner.getLookAngle();
        if (look.lengthSqr() < 1.0E-6D) {
            return null;
        }
        look = look.normalize();

        double range = Math.sqrt(Math.max(1.0D, MAX_DISTANCE_SQR));
        Vec3 eye = owner.getEyePosition();
        AABB searchBox = owner.getBoundingBox().expandTowards(look.scale(range)).inflate(SEARCH_PADDING);

        LivingEntity best = null;
        double bestScore = Double.MAX_VALUE;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, searchBox,
                entity -> entity != owner && TargetingUtil.isTargetableLiving(entity))) {
            Vec3 center = target.getBoundingBox().getCenter();
            Vec3 toTarget = center.subtract(eye);
            double along = toTarget.dot(look);
            if (along <= 0.35D || along > range) {
                continue;
            }

            Vec3 closestOnCrosshair = eye.add(look.scale(along));
            double crosshairDistanceSqr = closestOnCrosshair.distanceToSqr(center);
            double allowedRadius = Math.max(MIN_LOCK_RADIUS,
                    along * LOCK_CONE_TAN + target.getBbWidth() * 0.5D + TARGET_RADIUS_PADDING);
            if (crosshairDistanceSqr > allowedRadius * allowedRadius || !hasLineOfSight(level, owner, eye, center)) {
                continue;
            }

            double score = crosshairDistanceSqr + along * 0.001D;
            if (score < bestScore) {
                bestScore = score;
                best = target;
            }
        }
        return best;
    }

    private static boolean hasLineOfSight(Level level, LivingEntity owner, Vec3 start, Vec3 end) {
        HitResult result = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, owner));
        return result.getType() == HitResult.Type.MISS || result.getLocation().distanceToSqr(end) < 0.35D;
    }
}
