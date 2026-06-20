package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import com.rzy.dealt_force_skills.registry.ModEntities;
import com.rzy.dealt_force_skills.registry.ModSounds;
import com.rzy.dealt_force_skills.util.RangedSoundHelper;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.joml.Vector3f;

import java.util.UUID;

public class StingerSmokeDroneEntity extends Entity implements ItemSupplier {
    private static final int LIFE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.stingersmokedroneentity.life_ticks", 10 * 20);
    private static final int SMOKE_INTERVAL_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.stingersmokedroneentity.smoke_interval_ticks", 8);
    private static final double SPEED = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.stingersmokedroneentity.speed", 0.42D);
    private static final double GUIDE_DISTANCE = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.stingersmokedroneentity.guide_distance", 32.0D);
    private static final double GUIDE_ARRIVAL_DISTANCE_SQR = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.stingersmokedroneentity.guide_arrival_distance_sqr", 1.6D * 1.6D);
    private static final DustParticleOptions DRONE_DUST = new DustParticleOptions(new Vector3f(0.45f, 1.0f, 0.62f), 1.1f);

    private UUID ownerId;
    private boolean guided;
    private double directionX;
    private double directionY;
    private double directionZ = 1.0D;

    public StingerSmokeDroneEntity(EntityType<? extends StingerSmokeDroneEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public StingerSmokeDroneEntity(EntityType<? extends StingerSmokeDroneEntity> type, Level level, ServerPlayer owner, boolean guided) {
        this(type, level);
        ownerId = owner.getUUID();
        this.guided = guided;
        Vec3 look = owner.getLookAngle().normalize();
        directionX = look.x;
        directionY = look.y;
        directionZ = look.z;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.BEEHIVE);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnClientParticles();
            return;
        }
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }

        Entity owner = owner(serverLevel);
        if (owner == null || !owner.isAlive() || tickCount > LIFE_TICKS) {
            discard();
            return;
        }

        Vec3 direction = guided && owner instanceof ServerPlayer player
                ? guidedDirection(player)
                : storedDirection();
        rememberDirection(direction);
        Vec3 motion = direction.scale(SPEED);
        setDeltaMovement(motion);
        move(MoverType.SELF, motion);
        if (tickCount % SMOKE_INTERVAL_TICKS == 1) {
            spawnSmoke(serverLevel);
        }
        if (tickCount % 20 == 1) {
            RangedSoundHelper.playThrottled(serverLevel, position(), ModSounds.STINGER_DRONE_FLY.get(),
                    SoundSource.PLAYERS, 0.42f, 1.0f, 14.0D, 18, 5.0D);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        guided = tag.getBoolean("Guided");
        directionX = tag.getDouble("DirectionX");
        directionY = tag.getDouble("DirectionY");
        directionZ = tag.getDouble("DirectionZ");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putBoolean("Guided", guided);
        tag.putDouble("DirectionX", directionX);
        tag.putDouble("DirectionY", directionY);
        tag.putDouble("DirectionZ", directionZ);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static void stopGuidingFor(ServerPlayer owner) {
        if (!(owner.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID ownerUuid = owner.getUUID();
        Vec3 releaseDirection = owner.getLookAngle();
        if (releaseDirection.lengthSqr() > 0.0001D) {
            releaseDirection = releaseDirection.normalize();
        }
        AABB searchBox = owner.getBoundingBox().inflate(128.0D);
        for (StingerSmokeDroneEntity drone : serverLevel.getEntitiesOfClass(StingerSmokeDroneEntity.class, searchBox,
                drone -> drone.isAlive() && drone.guided && ownerUuid.equals(drone.ownerId))) {
            drone.guided = false;
            Vec3 flightDirection = drone.getDeltaMovement();
            drone.rememberDirection(flightDirection.lengthSqr() > 0.0001D ? flightDirection : releaseDirection);
        }
    }

    private Vec3 guidedDirection(ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        if (look.lengthSqr() < 0.0001D) {
            return storedDirection();
        }
        look = look.normalize();
        Vec3 guidePosition = player.getEyePosition().add(look.scale(GUIDE_DISTANCE));
        Vec3 toGuide = guidePosition.subtract(position());
        if (toGuide.lengthSqr() <= GUIDE_ARRIVAL_DISTANCE_SQR) {
            return look;
        }
        return toGuide.normalize();
    }

    private Vec3 storedDirection() {
        Vec3 direction = new Vec3(directionX, directionY, directionZ);
        return direction.lengthSqr() < 0.0001D ? new Vec3(0.0D, 0.0D, 1.0D) : direction.normalize();
    }

    private void rememberDirection(Vec3 direction) {
        if (direction.lengthSqr() < 0.0001D) {
            return;
        }
        Vec3 normalized = direction.normalize();
        directionX = normalized.x;
        directionY = normalized.y;
        directionZ = normalized.z;
    }

    private void spawnSmoke(ServerLevel level) {
        StingerSmokeCloudEntity cloud = new StingerSmokeCloudEntity(
                ModEntities.STINGER_SMOKE_CLOUD.get(),
                level,
                ownerId,
                StingerSmokeCloudEntity.DRONE_LIFE_TICKS,
                true
        );
        cloud.setPos(getX(), getY(), getZ());
        level.addFreshEntity(cloud);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(),
                60, StingerSmokeCloudEntity.RADIUS * 0.35D, 0.45D, StingerSmokeCloudEntity.RADIUS * 0.35D, 0.025D);
    }

    private Entity owner(ServerLevel level) {
        return ownerId == null ? null : level.getEntity(ownerId);
    }

    private void spawnClientParticles() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            return;
        }
        level().addParticle(DRONE_DUST, getX(), getY(), getZ(), 0.0D, 0.0D, 0.0D);
        if (tickCount % 3 == 0) {
            level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, getX(), getY(), getZ(), 0.0D, 0.01D, 0.0D);
        }
    }
}
