package com.rzy.dealt_force_skills.client.particle;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/**
 * One large, nearly static smoke billboard used by D-Wolf smoke clouds.
 * Cloud entities spawn a single particle every refresh window instead of
 * many drifting particles, cutting client particle cost while still
 * occluding vision inside the (slightly smaller) smoke radius.
 */
public class LargeSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseSize;

    public LargeSmokeParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
        }
        this.sprites = sprites;
        // Slightly longer than cloud refresh interval so coverage never gaps.
        this.lifetime = 22 + random.nextInt(6);
        this.alpha = 0.94F;
        // Single static billboard (perf). Phase 811 was ~5.2–6.0; enlarge +200% (x3) for FOV occlusion.
        this.baseSize = 15.6F + random.nextFloat() * 2.4F;
        this.quadSize = this.baseSize;
        this.setSpriteFromAge(sprites);
        this.gravity = 0.0F;
        // Nearly static: ignore incoming speed, tiny drift only.
        this.xd = 0.0D;
        this.yd = 0.002D;
        this.zd = 0.0D;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
            return;
        }
        super.tick();
        this.setSpriteFromAge(sprites);
        this.xd = 0.0D;
        this.yd = 0.002D;
        this.zd = 0.0D;
        float ageFraction = (float) this.age / (float) this.lifetime;
        // Stay large for FOV occlusion; only fade alpha near the end.
        this.quadSize = Mth.lerp(ageFraction, this.baseSize, this.baseSize * 0.92F);
        if (ageFraction > 0.75F) {
            this.alpha = Mth.lerp((ageFraction - 0.75F) / 0.25F, 0.94F, 0.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        int packed = 15728880;
        int j = packed & 255;
        int k = packed >> 16 & 255;
        j += 10;
        if (j > 240) j = 240;
        return j | k << 16;
    }
}
