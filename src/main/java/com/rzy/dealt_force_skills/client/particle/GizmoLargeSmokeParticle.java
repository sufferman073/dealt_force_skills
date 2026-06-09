package com.rzy.dealt_force_skills.client.particle;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public class GizmoLargeSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public GizmoLargeSmokeParticle(ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
        }
        this.sprites = sprites;
        this.lifetime = 45 + random.nextInt(25);
        this.alpha = 0.86F;
        this.quadSize = 5.4F + random.nextFloat() * 1.8F;
        this.setColor(1.0F, 0.78F + random.nextFloat() * 0.12F, 0.08F);
        this.setSpriteFromAge(sprites);
        this.gravity = -0.004F;
        this.xd = xSpeed * 0.25D + (random.nextDouble() - 0.5D) * 0.12D;
        this.zd = zSpeed * 0.25D + (random.nextDouble() - 0.5D) * 0.12D;
        this.yd = ySpeed * 0.35D + random.nextDouble() * 0.035D + 0.014D;
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
        this.quadSize = Mth.lerp(ageFraction, 6.2F, 2.7F);
        if (ageFraction > 0.68F) {
            this.alpha = Mth.lerp((ageFraction - 0.68F) / 0.32F, 0.86F, 0.0F);
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
