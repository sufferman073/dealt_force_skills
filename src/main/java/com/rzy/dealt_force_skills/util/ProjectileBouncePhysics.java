package com.rzy.dealt_force_skills.util;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

public final class ProjectileBouncePhysics {
    private static final double MAX_SPEED_RETENTION = 0.90D;

    private ProjectileBouncePhysics() {
    }

    public static Vec3 reflect(
            Direction surface,
            Vec3 velocity,
            double wallSpeedRetention,
            double groundSpeedRetention,
            double tangentialDamping) {
        Vec3 normal = Vec3.atLowerCornerOf(surface.getNormal());
        double normalSpeed = velocity.dot(normal);
        Vec3 reflected = normalSpeed < 0.0D
                ? velocity.subtract(normal.scale(2.0D * normalSpeed))
                : velocity;

        Vec3 reflectedNormal = normal.scale(reflected.dot(normal));
        double tangentRetention = clamp(tangentialDamping, 0.0D, 1.0D);
        Vec3 reflectedTangent = reflected.subtract(reflectedNormal).scale(tangentRetention);
        double speedRetention = clamp(
                surface == Direction.UP ? groundSpeedRetention : wallSpeedRetention,
                0.0D,
                MAX_SPEED_RETENTION);
        return reflectedNormal.add(reflectedTangent).scale(speedRetention);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
