package com.rzy.dealt_force_skills.character.raptor;

import net.minecraft.world.phys.Vec3;

public record RaptorFootprintMarker(
        int id,
        Vec3 position,
        int ageTicks,
        String ownerName,
        String equipmentSummary,
        boolean scanned
) {
}
