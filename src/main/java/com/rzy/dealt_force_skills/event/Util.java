package com.rzy.dealt_force_skills.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class Util {
    public static boolean isFromFront(Player player, Vec3 sourcePos, float degrees) {
        Vec3 forward = horizontalForward(player);
        Vec3 toSource = horizontalDirectionFromPlayer(player, sourcePos);
        if (toSource.lengthSqr() < 0.0001D) {
            return true;
        }
        return forward.dot(toSource.normalize()) > Math.cos(Math.toRadians(degrees));
    }

    public static boolean isFromBehind(Player player, Vec3 sourcePos, float degrees) {
        Vec3 forward = horizontalForward(player);
        Vec3 toSource = horizontalDirectionFromPlayer(player, sourcePos);
        if (toSource.lengthSqr() < 0.0001D) {
            return false;
        }
        return forward.dot(toSource.normalize()) < -Math.cos(Math.toRadians(degrees));
    }

    public static boolean isUpperBodySource(Player player, Entity source) {
        return source.getEyeY() >= player.getY() + 0.7;
    }

    private static Vec3 horizontalForward(Player player) {
        Vec3 look = player.getLookAngle();
        Vec3 horizontal = new Vec3(look.x, 0.0D, look.z);
        if (horizontal.lengthSqr() < 0.0001D) {
            horizontal = Vec3.directionFromRotation(0.0f, player.getYRot());
            horizontal = new Vec3(horizontal.x, 0.0D, horizontal.z);
        }
        return horizontal.normalize();
    }

    private static Vec3 horizontalDirectionFromPlayer(Player player, Vec3 sourcePos) {
        Vec3 bodyCenter = player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D);
        Vec3 toSource = sourcePos.subtract(bodyCenter);
        return new Vec3(toSource.x, 0.0D, toSource.z);
    }
}
