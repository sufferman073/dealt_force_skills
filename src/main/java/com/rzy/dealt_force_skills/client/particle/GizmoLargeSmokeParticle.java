package com.rzy.dealt_force_skills.client.particle;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

/** Single static large smoke billboard for Gizmo smoke clouds. */
public class GizmoLargeSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final float baseSize;

    public GizmoLargeSmokeParticle(ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
        }
        this.sprites = sprites;
        this.lifetime = 22 + random.nextInt(6);
        this.alpha = 0.90F;
        // Phase 811 was ~4.0–4.6; enlarge +200% (x3) for FOV occlusion, still one particle.
        this.baseSize = 12.0F + random.nextFloat() * 1.8F;
        this.quadSize = this.baseSize;
        this.setColor(1.0F, 0.78F + random.nextFloat() * 0.12F, 0.08F);
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
            this.alpha = Mth.lerp((ageFraction - 0.75F) / 0.25F, 0.90F, 0.0F);
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
