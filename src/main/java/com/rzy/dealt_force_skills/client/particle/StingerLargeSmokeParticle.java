package com.rzy.dealt_force_skills.client.particle;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/** Single static large smoke billboard for Stinger smoke clouds. */
public class StingerLargeSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseSize;

    public StingerLargeSmokeParticle(ClientLevel level, double x, double y, double z,
                                     double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
        }
        this.sprites = sprites;
        this.lifetime = 22 + random.nextInt(6);
        this.alpha = 0.92F;
        // Phase 811 was ~4.8–5.5; enlarge +200% (x3) for FOV occlusion, still one particle.
        this.baseSize = 14.4F + random.nextFloat() * 2.1F;
        this.quadSize = this.baseSize;
        float gray = 0.48F + random.nextFloat() * 0.16F;
        this.setColor(gray, gray, gray);
        this.setSpriteFromAge(sprites);
        this.gravity = 0.0F;
        this.xd = 0.0D;
        this.yd = 0.0015D;
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
        this.yd = 0.0015D;
        this.zd = 0.0D;
        float ageFraction = (float) this.age / (float) this.lifetime;
        this.quadSize = Mth.lerp(ageFraction, this.baseSize, this.baseSize * 0.92F);
        if (ageFraction > 0.75F) {
            this.alpha = Mth.lerp((ageFraction - 0.75F) / 0.25F, 0.92F, 0.0F);
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }
}
