package com.rzy.dealt_force_skills.character.gizmo;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class GizmoPlacementHelper {
    public static final double SMOKE_RANGE = 5.0D;
    public static final double SPIDER_RANGE = 3.5D;

    private GizmoPlacementHelper() {
    }

    public static Optional<Placement> findTrapPlacement(Player player, GizmoTool tool) {
        if (tool != GizmoTool.SMOKE_TRAP && tool != GizmoTool.SPIDER_NEST) {
            return Optional.empty();
        }

        Level level = player.level();
        double range = tool == GizmoTool.SMOKE_TRAP ? SMOKE_RANGE : SPIDER_RANGE;
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 0.0001D) {
            return Optional.empty();
        }

        Vec3 end = eye.add(look.normalize().scale(range));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getLocation().distanceTo(eye) > range + 0.15D) {
            return Optional.empty();
        }

        Direction face = hit.getDirection();
        if (tool == GizmoTool.SPIDER_NEST && face != Direction.UP) {
            return Optional.empty();
        }

        Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());
        Vec3 position = tool == GizmoTool.SPIDER_NEST
                ? new Vec3(hit.getLocation().x, hit.getBlockPos().getY() + 1.04D, hit.getLocation().z)
                : hit.getLocation().add(normal.scale(0.08D));
        return Optional.of(new Placement(position, face));
    }

    public record Placement(Vec3 position, Direction face) {
    }
}
