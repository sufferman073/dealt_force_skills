package com.rzy.dealt_force_skills.registry;

import com.rzy.dealt_force_skills.DealtForceSkillsMod;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES =
            DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, DealtForceSkillsMod.MODID);

    public static final RegistryObject<SimpleParticleType> D_WOLF_LARGE_SMOKE =
            PARTICLES.register("d_wolf_large_smoke", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> GIZMO_LARGE_SMOKE =
            PARTICLES.register("gizmo_large_smoke", () -> new SimpleParticleType(false));

    public static final RegistryObject<SimpleParticleType> TOXIK_LARGE_SMOKE =
            PARTICLES.register("toxik_large_smoke", () -> new SimpleParticleType(false));
}
