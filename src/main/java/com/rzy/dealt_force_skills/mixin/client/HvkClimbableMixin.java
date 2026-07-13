package com.rzy.dealt_force_skills.mixin.client;

import com.rzy.dealt_force_skills.client.HvkGlueClientCache;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Disables passive climbing on blocks glued by the HVK Universal Glue item. Registered client-only
 * (same reasoning as the mod's other movement mixins): player movement is client-predicted, so the
 * server never runs this and would desync if it did.
 * <p>
 * Once a block is part of a glued network it becomes a directional zipline (see
 * {@link com.rzy.dealt_force_skills.climb.HvkZiplineManager}) instead of a ladder: forcing
 * {@code onClimbable()} to {@code false} here overrides vanilla's own climbable check, so even a glued
 * ladder or vine can no longer be climbed by simply walking into it.
 */
@Mixin(LivingEntity.class)
public abstract class HvkClimbableMixin {

    @Inject(method = "onClimbable", at = @At("HEAD"), cancellable = true)
    private void dealt_force_skills$forceClimbableWhenGlued(CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (dealt_force_skills$isNearGluedPosition(self)) {
            cir.setReturnValue(false);
        }
    }

    private static boolean dealt_force_skills$isNearGluedPosition(LivingEntity entity) {
        Level level = entity.level();
        ResourceKey<Level> dimension = level.dimension();
        BlockPos base = entity.blockPosition();
        if (HvkGlueClientCache.isGlued(dimension, base)) {
            return true;
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (HvkGlueClientCache.isGlued(dimension, base.relative(direction))) {
                return true;
            }
        }
        return false;
    }
}
