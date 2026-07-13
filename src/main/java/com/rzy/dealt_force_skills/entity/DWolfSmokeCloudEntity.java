package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import com.rzy.dealt_force_skills.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class DWolfSmokeCloudEntity extends Entity implements ItemSupplier {
    public static volatile int LIFE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("LIFE_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.dwolfsmokecloudentity.life_ticks", 160));
    public static volatile double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("RADIUS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.dwolfsmokecloudentity.radius", 6.0));
    /** Client: refresh the single static billboard every N ticks. */
    private static final int PARTICLE_REFRESH_TICKS = 15;

    private UUID ownerId;
    private int lifeTicks = LIFE_TICKS;

    public DWolfSmokeCloudEntity(EntityType<? extends DWolfSmokeCloudEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public DWolfSmokeCloudEntity(EntityType<? extends DWolfSmokeCloudEntity> type, Level level, UUID ownerId) {
        this(type, level);
        this.ownerId = ownerId;
    }

    public void setLifeTicks(int lifeTicks) {
        this.lifeTicks = Math.max(1, lifeTicks);
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.GRAY_STAINED_GLASS);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnSmokeParticles();
            return;
        }

        if (level() instanceof ServerLevel serverLevel) {
            suppressTargetsThroughSmoke(serverLevel);
        }
        if (tickCount >= lifeTicks) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        lifeTicks = tag.contains("LifeTicks") ? Math.max(1, tag.getInt("LifeTicks")) : LIFE_TICKS;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("LifeTicks", lifeTicks);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void spawnSmokeParticles() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            return;
        }
        // One large static billboard at the cloud center — huge perf win vs multi-particle fog.
        if (tickCount > 1 && tickCount % PARTICLE_REFRESH_TICKS != 0) {
            return;
        }
        level().addParticle(ModParticles.D_WOLF_LARGE_SMOKE.get(),
                getX(), getY() + 1.5D, getZ(), 0.0D, 0.0D, 0.0D);
    }

    private void suppressTargetsThroughSmoke(ServerLevel level) {
        if (ownerId == null || !(level.getEntity(ownerId) instanceof LivingEntity owner) || !owner.isAlive()) {
            return;
        }

        AABB box = new AABB(position(), position()).inflate(40.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, mob -> mob.isAlive() && mob.getTarget() == owner)) {
            Vec3 from = mob.getEyePosition();
            Vec3 to = owner.getEyePosition();
            if (segmentIntersectsSmoke(from, to)) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
        }
    }

    private boolean segmentIntersectsSmoke(Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSqr = segment.lengthSqr();
        if (lengthSqr < 0.0001D) {
            return start.distanceTo(position()) <= RADIUS;
        }

        double t = position().subtract(start).dot(segment) / lengthSqr;
        t = Math.max(0.0D, Math.min(1.0D, t));
        Vec3 closest = start.add(segment.scale(t));
        return closest.distanceTo(position()) <= RADIUS;
    }
}
