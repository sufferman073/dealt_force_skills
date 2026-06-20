package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModSounds;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.UUID;

public class ToxikTearGasGrenadeEntity extends Projectile implements ItemSupplier {
    private static final int MAX_FLIGHT_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.toxikteargasgrenadeentity.max_flight_ticks", 80);
    private static final double BOUNCE_FACTOR = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.toxikteargasgrenadeentity.bounce_factor", 0.7D);
    private static final DustParticleOptions CYAN_TRAIL = new DustParticleOptions(new Vector3f(0.25f, 1.0f, 0.92f), 0.9f);

    private UUID ownerId;

    public ToxikTearGasGrenadeEntity(EntityType<? extends ToxikTearGasGrenadeEntity> type, Level level) {
        super(type, level);
    }

    public ToxikTearGasGrenadeEntity(EntityType<? extends ToxikTearGasGrenadeEntity> type, Level level, LivingEntity owner) {
        super(type, level);
        setOwner(owner);
        ownerId = owner.getUUID();
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.CYAN_DYE);
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
            if (ClientVisionHooks.isThermalVisionActive()) {
                return;
            }
            level().addParticle(CYAN_TRAIL, getX(), getY() + 0.08D, getZ(), 0.0D, 0.01D, 0.0D);
        } else if (tickCount > MAX_FLIGHT_TICKS) {
            burst(position());
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

    private void handleBlockHit(BlockHitResult hit) {
        Direction direction = hit.getDirection();
        if (direction == Direction.UP) {
            burst(hit.getLocation());
            return;
        }
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

    private void burst(Vec3 center) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            discard();
            return;
        }
        serverLevel.playSound(null, center.x, center.y, center.z, ModSounds.TOXIK_TEAR_GAS_BURST.get(),
                SoundSource.PLAYERS, 1.1f, 1.0f);
        serverLevel.sendParticles(ParticleTypes.CLOUD, center.x, center.y + 0.35D, center.z,
                180, ToxikTearGasCloudEntity.RADIUS * 0.52D, 1.0D, ToxikTearGasCloudEntity.RADIUS * 0.52D, 0.04D);
        ToxikTearGasCloudEntity cloud = new ToxikTearGasCloudEntity(
                ModEntities.TOXIK_TEAR_GAS_CLOUD.get(), serverLevel, ownerId, ToxikTearGasCloudEntity.LIFE_TICKS);
        cloud.setPos(center.x, center.y, center.z);
        serverLevel.addFreshEntity(cloud);
        discard();
    }
}
