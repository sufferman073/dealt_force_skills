package com.rzy.dealt_force_skills.character.shepherd;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class ShepherdPlacementHelper {
    public static final double SONIC_TRAP_RANGE = 8.0D;
    private static final double TRAP_WIDTH = 0.38D;
    private static final double TRAP_HEIGHT = 0.22D;
    private static final double SURFACE_GAP = 0.025D;

    private ShepherdPlacementHelper() {
    }

    public static Optional<Placement> findSonicTrapPlacement(Player player) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 0.0001D) {
            return Optional.empty();
        }

        Vec3 end = eye.add(look.normalize().scale(SONIC_TRAP_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getLocation().distanceTo(eye) > SONIC_TRAP_RANGE + 0.15D) {
            return Optional.empty();
        }

        Direction face = hit.getDirection();
        return Optional.of(new Placement(positionOutsideSurface(hit.getLocation(), face), face));
    }

    private static Vec3 positionOutsideSurface(Vec3 hit, Direction face) {
        return switch (face) {
            case UP -> new Vec3(hit.x, hit.y + SURFACE_GAP, hit.z);
            case DOWN -> new Vec3(hit.x, hit.y - TRAP_HEIGHT - SURFACE_GAP, hit.z);
            case NORTH, SOUTH -> new Vec3(
                    hit.x,
                    hit.y - TRAP_HEIGHT * 0.5D,
                    hit.z + face.getStepZ() * (TRAP_WIDTH * 0.5D + SURFACE_GAP)
            );
            case EAST, WEST -> new Vec3(
                    hit.x + face.getStepX() * (TRAP_WIDTH * 0.5D + SURFACE_GAP),
                    hit.y - TRAP_HEIGHT * 0.5D,
                    hit.z
            );
        };
    }

    public record Placement(Vec3 position, Direction face) {
    }
}
