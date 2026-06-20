package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import com.rzy.dealt_force_skills.network.NetworkHandler;
import com.rzy.dealt_force_skills.network.S2C_GizmoRevealEntities;
import com.rzy.dealt_force_skills.registry.ModEffects;
import com.rzy.dealt_force_skills.registry.ModParticles;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GizmoSmokeCloudEntity extends Entity implements ItemSupplier {
    public static final int LIFE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.gizmosmokecloudentity.life_ticks", 15 * 20);
    public static final double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.gizmosmokecloudentity.radius", 5.625D);
    private static final int LARGE_SMOKE_PARTICLES_PER_TICK = com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.gizmosmokecloudentity.large_smoke_particles_per_tick", 7);

    private UUID ownerId;

    public GizmoSmokeCloudEntity(EntityType<? extends GizmoSmokeCloudEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public GizmoSmokeCloudEntity(EntityType<? extends GizmoSmokeCloudEntity> type, Level level, UUID ownerId) {
        this(type, level);
        this.ownerId = ownerId;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(Items.YELLOW_DYE);
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
            applySmokeEffects(serverLevel);
            suppressTargetsThroughSmoke(serverLevel);
        }
        if (tickCount >= LIFE_TICKS) {
            discard();
        }
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

    private void applySmokeEffects(ServerLevel level) {
        AABB box = new AABB(position(), position()).inflate(RADIUS);
        List<Integer> revealIds = new ArrayList<>();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, LivingEntity::isAlive)) {
            if (ownerId != null && target.getUUID().equals(ownerId)) {
                continue;
            }
            if (target.position().add(0.0D, target.getBbHeight() * 0.5D, 0.0D).distanceTo(position()) > RADIUS) {
                continue;
            }

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.gizmo_smoke_cloud_entity.effect.movement_slowdown.0.duration_ticks", 20), com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.gizmo_smoke_cloud_entity.effect.movement_slowdown.0.amplifier", 0), false, true, true));
            if (target.hasEffect(ModEffects.CORROSION.get())) {
                revealIds.add(target.getId());
            }
        }

        if (!revealIds.isEmpty() && tickCount % 5 == 0 && ownerId != null && level.getEntity(ownerId) instanceof ServerPlayer owner) {
            NetworkHandler.sendToPlayer(new S2C_GizmoRevealEntities(revealIds), owner);
        }
    }

    private void suppressTargetsThroughSmoke(ServerLevel level) {
        if (ownerId == null || !(level.getEntity(ownerId) instanceof LivingEntity owner) || !owner.isAlive()) {
            return;
        }

        AABB box = new AABB(position(), position()).inflate(40.0D);
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, mob -> mob.isAlive() && mob.getTarget() == owner)) {
            if (segmentIntersectsSmoke(mob.getEyePosition(), owner.getEyePosition())) {
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

    private void spawnSmokeParticles() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            return;
        }
        for (int i = 0; i < LARGE_SMOKE_PARTICLES_PER_TICK; i++) {
            double angle = random.nextDouble() * Math.PI * 2.0D;
            double dist = Math.sqrt(random.nextDouble()) * RADIUS * 0.82D;
            double x = getX() + Math.cos(angle) * dist;
            double z = getZ() + Math.sin(angle) * dist;
            double y = getY() + 0.2D + random.nextDouble() * 3.6D;
            double xSpeed = (random.nextDouble() - 0.5D) * 0.014D;
            double zSpeed = (random.nextDouble() - 0.5D) * 0.014D;
            level().addParticle(ModParticles.GIZMO_LARGE_SMOKE.get(), x, y, z, xSpeed, 0.008D, zSpeed);
        }
    }
}
