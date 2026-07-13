package com.rzy.dealt_force_skills.character.department;

import com.rzy.dealt_force_skills.config.DealtForceConfig;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class DepartmentPlacementHelper {
    public static volatile double TRAP_RANGE = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("TRAP_RANGE", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.department.department_placement_helper.trap_range", 20.0));
    private static volatile double TRAP_WIDTH = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("TRAP_WIDTH", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.department.department_placement_helper.trap_width", 0.42));
    private static volatile double TRAP_HEIGHT = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("TRAP_HEIGHT", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.department.department_placement_helper.trap_height", 0.36));
    private static volatile double SURFACE_GAP = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("SURFACE_GAP", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("characters.department.department_placement_helper.surface_gap", 0.025));
    private DepartmentPlacementHelper() {
    }

    public static Optional<Placement> findTrapPlacement(Player player) {
        Level level = player.level();
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 0.0001D) {
            return Optional.empty();
        }

        Vec3 end = eye.add(look.normalize().scale(TRAP_RANGE));
        BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        if (hit.getType() != HitResult.Type.BLOCK || hit.getLocation().distanceTo(eye) > TRAP_RANGE + 0.15D) {
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
