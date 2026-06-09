package com.rzy.dealt_force_skills.client.particle;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/**
 * A single smoke particle rendered at ~7-block scale so that just 3-5 per tick
 * can cover the D-Wolf smoke cloud radius (7.5 blocks) with dense coverage,
 * matching the AI-suppression radius for logical consistency.
 */
public class LargeSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public LargeSmokeParticle(ClientLevel level, double x, double y, double z,
                                 double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
        }
        this.sprites = sprites;
        this.lifetime = 50 + random.nextInt(30);  // 2.5 – 4 seconds
        this.alpha = 0.92F;
        this.quadSize = 7.0F + random.nextFloat() * 2.5F; // 7–9.5 blocks across
        this.setSpriteFromAge(sprites);
        this.gravity = -0.005F;  // very slow rise
        this.xd = xSpeed * 0.2 + (random.nextDouble() - 0.5) * 0.25;
        this.zd = zSpeed * 0.2 + (random.nextDouble() - 0.5) * 0.25;
        this.yd = ySpeed * 0.4 + random.nextDouble() * 0.06 + 0.02;
    }

    @Override
    public void tick() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
            return;
        }
        super.tick();
        this.setSpriteFromAge(sprites);
        float ageFraction = (float) this.age / (float) this.lifetime;
        this.quadSize = Mth.lerp(ageFraction, 8.0F, 3.5F);
        if (ageFraction > 0.7F) {
            this.alpha = Mth.lerp((ageFraction - 0.7F) / 0.3F, 0.92F, 0.0F);
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
