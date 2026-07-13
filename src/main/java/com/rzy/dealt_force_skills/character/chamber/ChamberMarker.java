package com.rzy.dealt_force_skills.character.chamber;

import net.minecraft.world.phys.Vec3;

public record ChamberMarker(int entityId, ChamberMarkerType type, Vec3 position) {
}
