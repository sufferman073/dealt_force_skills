package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.UUID;

public class MorseShockOrbEntity extends Projectile implements ItemSupplier {
    private static final double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.morseshockorbentity.radius", 7.0D);
    private static final int STRONG_SHOCK_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.morseshockorbentity.strong_shock_ticks", 4 * 20);
    private static final int STRONG_SHOCK_AMPLIFIER = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.morse_shock_orb_entity.strong_shock_amplifier", 0);
    private static final DustParticleOptions SHOCK_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.24f), 1.2f);

    private UUID ownerId;

    public MorseShockOrbEntity(EntityType<? extends MorseShockOrbEntity> type, Level level) {
        super(type, level);
    }

    public MorseShockOrbEntity(EntityType<? extends MorseShockOrbEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
        ownerId = owner.getUUID();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.ECHO_SHARD);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 motion = getDeltaMovement();
        Vec3 next = position().add(motion);
        HitResult hit = level().clip(new ClipContext(position(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (!level().isClientSide && hit.getType() != HitResult.Type.MISS) {
            burst(hit.getLocation());
            return;
        }

        if (!level().isClientSide) {
            LivingEntity entityHit = firstEntityHit(motion);
            if (entityHit != null) {
                burst(entityHit.position().add(0.0D, entityHit.getBbHeight() * 0.5D, 0.0D));
                return;
            }
        }

        setPos(next.x, next.y, next.z);
        setDeltaMovement(motion.add(0.0D, -0.045D, 0.0D).scale(0.985D));
        checkInsideBlocks();
        if (level().isClientSide) {
            level().addParticle(SHOCK_DUST, getX(), getY() + 0.08D, getZ(), 0.0D, 0.01D, 0.0D);
        }
        if (!level().isClientSide && tickCount > 20 * 10) {
            discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target instanceof LivingEntity && target != getOwner();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private LivingEntity firstEntityHit(Vec3 motion) {
        AABB box = getBoundingBox().expandTowards(motion).inflate(0.35D);
        return level().getEntitiesOfClass(LivingEntity.class, box,
                        target -> target != getOwner() && TargetingUtil.isTargetableLiving(target))
                .stream()
                .findFirst()
                .orElse(null);
    }

    private void burst(Vec3 center) {
        if (!(level() instanceof ServerLevel level)) {
            discard();
            return;
        }
        LivingEntity owner = owner(level);
        level.playSound(null, center.x, center.y, center.z, ModSounds.MORSE_SHOCK_ORB_BURST.get(),
                SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.SONIC_BOOM, center.x, center.y + 0.2D, center.z,
                1, 0.0D, 0.0D, 0.0D, 0.0D);
        level.sendParticles(SHOCK_DUST, center.x, center.y + 0.35D, center.z,
                120, RADIUS * 0.35D, RADIUS * 0.18D, RADIUS * 0.35D, 0.04D);

        AABB box = new AABB(center, center).inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (!TargetingUtil.isTargetableLiving(target) || (owner != null && target.getUUID().equals(owner.getUUID()))) {
                continue;
            }
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (targetCenter.distanceTo(center) > RADIUS) {
                continue;
            }
            target.addEffect(new MobEffectInstance(ModEffects.MORSE_STRONG_SHOCK.get(),
                    STRONG_SHOCK_TICKS, STRONG_SHOCK_AMPLIFIER, false, true, true), owner);
            level.playSound(null, target.blockPosition(), target instanceof net.minecraft.world.entity.player.Player
                            ? ModSounds.MORSE_DEAFENED.get()
                            : ModSounds.MORSE_AI_DISABLED.get(),
                    SoundSource.PLAYERS, 0.65f, 1.0f);
        }
        discard();
    }

    private LivingEntity owner(ServerLevel level) {
        if (getOwner() instanceof LivingEntity living) {
            return living;
        }
        Entity owner = ownerId == null ? null : level.getEntity(ownerId);
        return owner instanceof LivingEntity living ? living : null;
    }
}
