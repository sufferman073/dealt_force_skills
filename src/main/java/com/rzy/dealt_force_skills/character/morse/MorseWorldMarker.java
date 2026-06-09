package com.rzy.dealt_force_skills.character.morse;

import net.minecraft.world.phys.Vec3;

public record MorseWorldMarker(MorseMarkerType type, int entityId, Vec3 position, int ticks) {
}
