package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.skill.SkillDamageHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

public class DWolfHandCannonGrenadeEntity extends Projectile implements ItemSupplier {
    private static final int FUSE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.dwolfhandcannongrenadeentity.fuse_ticks", 70);
    private static final double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.dwolfhandcannongrenadeentity.radius", 6.0D);
    private static final double BOUNCE_FACTOR = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.dwolfhandcannongrenadeentity.bounce_factor", 0.62D);
    private static final double GROUND_REST_OFFSET = 0.24D;

    private int fuseRemaining = FUSE_TICKS;
    private boolean stopped;
    private int lastImpactTick = -1000;

    public DWolfHandCannonGrenadeEntity(EntityType<? extends DWolfHandCannonGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public DWolfHandCannonGrenadeEntity(EntityType<? extends DWolfHandCannonGrenadeEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.FIRE_CHARGE);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide) {
            fuseRemaining--;
            if (fuseRemaining <= 0) {
                explode(position());
                return;
            }
        }

        if (stopped) {
            spawnClientTrail();
            return;
        }

        Vec3 motion = getDeltaMovement();
        Vec3 next = position().add(motion);
        HitResult hit = level().clip(new ClipContext(position(), next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            handleBlockHit((BlockHitResult) hit);
            return;
        }

        setPos(next.x, next.y, next.z);
        setDeltaMovement(motion.add(0.0D, -0.045D, 0.0D).scale(0.985D));
        checkInsideBlocks();
        spawnClientTrail();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        fuseRemaining = tag.getInt("FuseRemaining");
        if (fuseRemaining <= 0) {
            fuseRemaining = FUSE_TICKS;
        }
        stopped = tag.getBoolean("Stopped");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("FuseRemaining", fuseRemaining);
        tag.putBoolean("Stopped", stopped);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void handleBlockHit(BlockHitResult hit) {
        lastImpactTick = tickCount;
        Direction direction = hit.getDirection();

        if (direction == Direction.UP) {
            // Snap to ground surface like incendiary grenade - sit on top of the block
            double groundY = hit.getBlockPos().getY() + 1.0D + GROUND_REST_OFFSET;
            setPos(hit.getLocation().x, groundY, hit.getLocation().z);
            stopped = true;
            setDeltaMovement(Vec3.ZERO);
        } else {
            Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());
            setPos(hit.getLocation().x + normal.x * 0.04D,
                    hit.getLocation().y + normal.y * 0.04D,
                    hit.getLocation().z + normal.z * 0.04D);
            setDeltaMovement(bounce(direction, getDeltaMovement()));
        }

        if (level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, hit.getLocation().x, hit.getLocation().y, hit.getLocation().z,
                    ModSounds.D_WOLF_HAND_CANNON_IMPACT.get(), SoundSource.PLAYERS, 0.75f, 1.0f);
        }
    }

    private Vec3 bounce(Direction direction, Vec3 motion) {
        return switch (direction.getAxis()) {
            case X -> new Vec3(-motion.x * BOUNCE_FACTOR, motion.y * 0.82D, motion.z * BOUNCE_FACTOR);
            case Y -> new Vec3(motion.x * BOUNCE_FACTOR, -motion.y * 0.45D, motion.z * BOUNCE_FACTOR);
            case Z -> new Vec3(motion.x * BOUNCE_FACTOR, motion.y * 0.82D, -motion.z * BOUNCE_FACTOR);
        };
    }

    private void explode(Vec3 center) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }

        LivingEntity owner = getOwner() instanceof LivingEntity living ? living : null;
        serverLevel.playSound(null, center.x, center.y, center.z, ModSounds.D_WOLF_HAND_CANNON_EXPLODE.get(),
                SoundSource.PLAYERS, 1.3f, 1.0f);
        serverLevel.sendParticles(ParticleTypes.EXPLOSION, center.x, center.y + 0.2D, center.z,
                8, 0.45D, 0.25D, 0.45D, 0.02D);
        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.2D, center.z,
                48, RADIUS * 0.65D, 0.35D, RADIUS * 0.65D, 0.04D);

        AABB box = new AABB(center, center).inflate(RADIUS);
        for (LivingEntity target : serverLevel.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            double distance = target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).distanceTo(center);
            if (distance > RADIUS) {
                continue;
            }

            float baseDamage = target instanceof Player ? 12.0f : 24.0f;
            float amount = baseDamage * Math.max(0.0f, 1.0f - (float) distance * 0.1f);
            if (amount <= 0.0f) {
                continue;
            }

            Vec3 before = target.getDeltaMovement();
            target.invulnerableTime = 0;
            SkillDamageHelper.hurt(target, SkillDamageHelper.dWolfHandCannon(serverLevel, this, owner), owner, amount);
            target.setDeltaMovement(before);
            target.hurtMarked = true;
        }
        discard();
    }

    private void spawnClientTrail() {
        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.04D, getZ(), 0.0D, 0.01D, 0.0D);
        }
    }

    public boolean isStoppedForRender() {
        return stopped;
    }

    public float impactAnimationSeconds(float partialTick) {
        return (tickCount - lastImpactTick + partialTick) / 20.0F;
    }
}
