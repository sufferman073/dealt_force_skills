package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.character.electronics.ElectronicInterferenceManager;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import com.rzy.dealt_force_skills.util.TargetingUtil;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.UUID;

public class RaptorPulseGrenadeEntity extends Projectile implements ItemSupplier {
    public static final int FUSE_TICKS = 3 * 20;
    public static final double RADIUS = 10.0D;
    public static final int ACTION_PAUSE_TICKS = 8;
    public static final int EMI_TICKS = 20 * 20;
    private static final double BOUNCE_FACTOR = 0.7D;
    private static final DustParticleOptions BLUE_TRAIL = new DustParticleOptions(new Vector3f(0.35f, 0.56f, 1.0f), 1.0f);
    private static final DustParticleOptions BLUE_BURST = new DustParticleOptions(new Vector3f(0.48f, 0.76f, 1.0f), 1.6f);

    private UUID ownerId;

    public RaptorPulseGrenadeEntity(EntityType<? extends RaptorPulseGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public RaptorPulseGrenadeEntity(EntityType<? extends RaptorPulseGrenadeEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
        ownerId = owner.getUUID();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.LIGHT_BLUE_DYE);
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
        if (!level().isClientSide && hit.getType() == HitResult.Type.BLOCK) {
            handleBlockHit((BlockHitResult) hit);
            return;
        }

        setPos(next.x, next.y, next.z);
        setDeltaMovement(motion.add(0.0D, -0.045D, 0.0D).scale(0.985D));
        checkInsideBlocks();

        if (level().isClientSide) {
            level().addParticle(BLUE_TRAIL, getX(), getY() + 0.08D, getZ(), 0.0D, 0.01D, 0.0D);
        } else {
            if (tickCount % 20 == 1 && level() instanceof ServerLevel level) {
                level.playSound(null, blockPosition(), ModSounds.RAPTOR_PULSE_GRENADE_COUNTDOWN.get(),
                        SoundSource.PLAYERS, 0.45f, 1.0f + tickCount / 60.0f);
            }
            if (tickCount >= FUSE_TICKS) {
                pulseAt((ServerLevel) level(), position(), owner((ServerLevel) level()), this);
                discard();
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
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

    public static void pulseAt(ServerLevel level, Vec3 center, LivingEntity owner, Entity directEntity) {
        level.playSound(null, center.x, center.y, center.z, ModSounds.RAPTOR_PULSE_GRENADE_EXPLODE.get(),
                SoundSource.PLAYERS, 1.1f, 1.0f);
        level.sendParticles(ParticleTypes.ELECTRIC_SPARK, center.x, center.y + 0.4D, center.z,
                180, RADIUS * 0.45D, RADIUS * 0.25D, RADIUS * 0.45D, 0.12D);
        level.sendParticles(BLUE_BURST, center.x, center.y + 0.4D, center.z,
                120, RADIUS * 0.42D, RADIUS * 0.18D, RADIUS * 0.42D, 0.0D);
        ElectronicInterferenceManager.disruptElectronicDevices(level, center, RADIUS);

        AABB box = new AABB(center, center).inflate(RADIUS);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (!TargetingUtil.isTargetableLiving(target)
                    || (owner != null && target.getUUID().equals(owner.getUUID()))) {
                continue;
            }
            Vec3 targetCenter = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D);
            if (targetCenter.distanceTo(center) > RADIUS) {
                continue;
            }
            target.invulnerableTime = 0;
            SkillDamageHelper.hurtUnscaled(target, SkillDamageHelper.raptorPulse(level, directEntity, owner), 1.0f);
            target.addEffect(new MobEffectInstance(ModEffects.RAPTOR_ACTION_PAUSE.get(),
                    ACTION_PAUSE_TICKS, 0, false, true, true), owner);
            target.addEffect(new MobEffectInstance(ModEffects.RAPTOR_ELECTROMAGNETIC_INTERFERENCE.get(),
                    EMI_TICKS, 0, false, true, true), owner);
            level.playSound(null, target.blockPosition(), ModSounds.RAPTOR_PULSE_STAGGER_HIT.get(),
                    SoundSource.PLAYERS, 0.55f, 1.0f);
        }
    }

    private void handleBlockHit(BlockHitResult hit) {
        Direction direction = hit.getDirection();
        Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
        setPos(hit.getLocation().x + normal.x * 0.04D,
                hit.getLocation().y + normal.y * 0.04D,
                hit.getLocation().z + normal.z * 0.04D);
        setDeltaMovement(bounce(direction, getDeltaMovement()));
    }

    private Vec3 bounce(Direction direction, Vec3 motion) {
        return switch (direction.getAxis()) {
            case X -> new Vec3(-motion.x * BOUNCE_FACTOR, motion.y * 0.86D, motion.z * BOUNCE_FACTOR);
            case Y -> new Vec3(motion.x * BOUNCE_FACTOR, -motion.y * 0.45D, motion.z * BOUNCE_FACTOR);
            case Z -> new Vec3(motion.x * BOUNCE_FACTOR, motion.y * 0.86D, -motion.z * BOUNCE_FACTOR);
        };
    }

    private LivingEntity owner(ServerLevel level) {
        return ownerId == null || !(level.getEntity(ownerId) instanceof LivingEntity owner) ? null : owner;
    }
}
