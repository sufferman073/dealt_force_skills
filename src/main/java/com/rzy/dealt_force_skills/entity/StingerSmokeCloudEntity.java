package com.rzy.dealt_force_skills.entity;

import com.rzy.dealt_force_skills.util.ClientVisionHooks;
import com.rzy.dealt_force_skills.character.stinger.StingerStateManager;
import com.rzy.dealt_force_skills.registry.ModParticles;
import com.rzy.dealt_force_skills.team.DealtTeamManager;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.UUID;

public class StingerSmokeCloudEntity extends Entity implements ItemSupplier {
    public static volatile int GRENADE_LIFE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("GRENADE_LIFE_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.stingersmokecloudentity.grenade_life_ticks", 300));
    public static volatile int DRONE_LIFE_TICKS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("DRONE_LIFE_TICKS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.intValue("summons.stingersmokecloudentity.drone_life_ticks", 500));
    public static volatile double RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("RADIUS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.stingersmokecloudentity.radius", 5.5));
    public static volatile double ENHANCED_RADIUS = com.rzy.dealt_force_skills.config.DealtForceConfig.bind("ENHANCED_RADIUS", () -> com.rzy.dealt_force_skills.config.DealtForceConfig.doubleValue("summons.stingersmokecloudentity.enhanced_radius", 7.0));
    private static final int PARTICLE_REFRESH_TICKS = 15;
    private static final EntityDataAccessor<Boolean> DATA_ENHANCED = SynchedEntityData.defineId(StingerSmokeCloudEntity.class, EntityDataSerializers.BOOLEAN);

    private UUID ownerId;
    private int lifeTicks = GRENADE_LIFE_TICKS;
    private boolean falling;

    public StingerSmokeCloudEntity(EntityType<? extends StingerSmokeCloudEntity> type, Level level) {
        super(type, level);
        noPhysics = true;
    }

    public StingerSmokeCloudEntity(EntityType<? extends StingerSmokeCloudEntity> type, Level level, UUID ownerId, int lifeTicks, boolean falling) {
        this(type, level);
        this.ownerId = ownerId;
        this.lifeTicks = lifeTicks;
        this.falling = falling;
    }

    @Override
    public ItemStack getItem() {
        return new ItemStack(isEnhanced() ? Items.LIME_STAINED_GLASS : Items.GRAY_STAINED_GLASS);
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(DATA_ENHANCED, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            spawnSmokeParticles();
            return;
        }

        if (level() instanceof ServerLevel serverLevel) {
            if (falling) {
                settleDown(serverLevel);
            }
            if (isEnhanced()) {
                applySmokeRegen(serverLevel);
            }
            suppressTargetsThroughSmoke(serverLevel);
        }
        if (tickCount >= lifeTicks) {
            discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        ownerId = tag.hasUUID("Owner") ? tag.getUUID("Owner") : null;
        lifeTicks = tag.getInt("LifeTicks");
        if (lifeTicks <= 0) {
            lifeTicks = GRENADE_LIFE_TICKS;
        }
        setEnhanced(tag.getBoolean("Enhanced"));
        falling = tag.getBoolean("Falling");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if (ownerId != null) {
            tag.putUUID("Owner", ownerId);
        }
        tag.putInt("LifeTicks", lifeTicks);
        tag.putBoolean("Enhanced", isEnhanced());
        tag.putBoolean("Falling", falling);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public static void enhanceSmokeNear(ServerLevel level, Vec3 position) {
        AABB box = new AABB(position, position).inflate(RADIUS);
        for (StingerSmokeCloudEntity cloud : level.getEntitiesOfClass(StingerSmokeCloudEntity.class, box,
                cloud -> cloud.isAlive() && cloud.position().distanceTo(position) <= RADIUS)) {
            cloud.setEnhanced(true);
        }
    }

    private void applySmokeRegen(ServerLevel level) {
        double radius = smokeRadius();
        AABB box = new AABB(position(), position()).inflate(radius);
        ServerPlayer owner = ownerId == null ? null : level.getServer().getPlayerList().getPlayer(ownerId);
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, box,
                player -> player.isAlive()
                        && (owner == null || DealtTeamManager.isSelfOrTeammate(owner, player))
                        && player.position().add(0.0D, player.getBbHeight() * 0.5D, 0.0D).distanceTo(position()) <= radius)) {
            StingerStateManager.applySmokeRegen(owner, player);
        }
    }

    private void suppressTargetsThroughSmoke(ServerLevel level) {
        if (ownerId == null || !(level.getEntity(ownerId) instanceof LivingEntity owner) || !owner.isAlive()) {
            return;
        }

        AABB box = new AABB(position(), position()).inflate(40.0D);
        double radius = smokeRadius();
        boolean ownerInside = owner.position().distanceTo(position()) <= radius;
        for (Mob mob : level.getEntitiesOfClass(Mob.class, box, mob -> mob.isAlive() && mob.getTarget() == owner)) {
            boolean mobInside = mob.position().distanceTo(position()) <= radius;
            if (ownerInside && mobInside) {
                continue;
            }
            if (segmentIntersectsSmoke(mob.getEyePosition(), owner.getEyePosition())) {
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
        }
    }

    private boolean segmentIntersectsSmoke(Vec3 start, Vec3 end) {
        Vec3 segment = end.subtract(start);
        double lengthSqr = segment.lengthSqr();
        double radius = smokeRadius();
        if (lengthSqr < 0.0001D) {
            return start.distanceTo(position()) <= radius;
        }

        double t = position().subtract(start).dot(segment) / lengthSqr;
        t = Math.max(0.0D, Math.min(1.0D, t));
        Vec3 closest = start.add(segment.scale(t));
        return closest.distanceTo(position()) <= radius;
    }

    private void settleDown(ServerLevel level) {
        Vec3 start = position();
        Vec3 end = start.add(0.0D, -0.16D, 0.0D);
        HitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.BLOCK) {
            falling = false;
            return;
        }
        setPos(getX(), getY() - 0.05D, getZ());
    }

    private void spawnSmokeParticles() {
        if (ClientVisionHooks.isThermalVisionActive()) {
            return;
        }
        if (tickCount > 1 && tickCount % PARTICLE_REFRESH_TICKS != 0) {
            return;
        }
        boolean enhanced = isEnhanced();
        var particle = enhanced ? ModParticles.STINGER_HEALING_SMOKE.get() : ModParticles.STINGER_LARGE_SMOKE.get();
        level().addParticle(particle, getX(), getY() + 1.5D, getZ(), 0.0D, 0.0D, 0.0D);
    }

    private boolean isEnhanced() {
        return entityData.get(DATA_ENHANCED);
    }

    private void setEnhanced(boolean enhanced) {
        entityData.set(DATA_ENHANCED, enhanced);
    }

    private double smokeRadius() {
        return isEnhanced() ? ENHANCED_RADIUS : RADIUS;
    }
}
