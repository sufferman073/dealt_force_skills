package com.rzy.dealt_force_skills.character.vlinder;

import net.minecraft.world.phys.Vec3;

public record VlinderWorldMarker(
        VlinderMarkerType type,
        int entityId,
        Vec3 position,
        int remainingTicks,
        int progressTicks,
        int requiredTicks
) {
}
