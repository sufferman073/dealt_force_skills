package com.rzy.dealt_force_skills.client.particle;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.util.Mth;

public class ToxikLargeSmokeParticle extends TextureSheetParticle {
    private final SpriteSet sprites;

    public ToxikLargeSmokeParticle(ClientLevel level, double x, double y, double z,
                                   double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
        super(level, x, y, z, xSpeed, ySpeed, zSpeed);
        if (ClientVisionHooks.isThermalVisionActive()) {
            this.remove();
        }
        this.sprites = sprites;
        this.lifetime = 55 + random.nextInt(35);
        this.alpha = 0.88F;
        this.quadSize = 7.2F + random.nextFloat() * 2.4F;
        this.setColor(0.18F + random.nextFloat() * 0.08F,
                0.95F + random.nextFloat() * 0.05F,
                0.88F + random.nextFloat() * 0.10F);
        this.setSpriteFromAge(sprites);
        this.gravity = -0.004F;
        this.xd = xSpeed * 0.20D + (random.nextDouble() - 0.5D) * 0.16D;
        this.zd = zSpeed * 0.20D + (random.nextDouble() - 0.5D) * 0.16D;
        this.yd = ySpeed * 0.35D + random.nextDouble() * 0.045D + 0.012D;
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
        this.quadSize = Mth.lerp(ageFraction, 8.4F, 3.2F);
        if (ageFraction > 0.72F) {
            this.alpha = Mth.lerp((ageFraction - 0.72F) / 0.28F, 0.88F, 0.0F);
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
